/***********************************************************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************/
package oog.ui.viewer;

import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.ImageLoaderEvent;
import org.eclipse.swt.graphics.ImageLoaderListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ImageAnalyzer extends Composite implements IImageZoom {

	private List<ImageAnalyzerSelectionListener> listeners = new ArrayList<ImageAnalyzerSelectionListener>();

	public void addImageAnalyzerSelectionListener(ImageAnalyzerSelectionListener listener) {
		listeners.add(listener);
	}

	Map map = new Map();

	static ResourceBundle bundle = ResourceBundle.getBundle("examples_images");

	Display display;

	Canvas imageCanvas;

	StyledText dataText;

	Sash sash;

	Color whiteColor, blackColor, redColor, greenColor, blueColor, canvasBackground;

	Font fixedWidthFont;

	Cursor crossCursor;

	GC imageCanvasGC;

	int paletteWidth = 140; // recalculated and used as a width hint

	int ix = 0, iy = 0, py = 0; // used to scroll the image and palette

	float xscale = 1, yscale = 1; // used to scale the image

	int alpha = 255; // used to modify the alpha value of the image

	boolean incremental = false; // used to incrementally display an image

	boolean transparent = true; // used to display an image with transparency

	boolean showMask = false; // used to display an icon mask or transparent image mask

	boolean showBackground = false; // used to display the background of an animated image

	boolean animate = false; // used to animate a multi-image file

	Thread animateThread; // draws animated images

	Thread incrementalThread; // draws incremental images

	String lastPath; // used to seed the file dialog

	String currentName; // the current image file or URL name

	String fileName; // the current image file

	ImageLoader loader; // the loader for the current image file

	ImageData[] imageDataArray; // all image data read from the current file

	int imageDataIndex; // the index of the current image data

	ImageData imageData; // the currently-displayed image data

	Image image; // the currently-displayed image

	Vector incrementalEvents; // incremental image events

	long loadTime = 0; // the time it took to load the current image

	static final int INDEX_DIGITS = 4;

	static final int ALPHA_CONSTANT = 0;

	static final int ALPHA_X = 1;

	static final int ALPHA_Y = 2;

	class TextPrompter extends Dialog {
		String message = "";

		String result = null;

		Shell dialog;

		Text text;

		public TextPrompter(Shell parent, int style) {
			super(parent, style);
		}

		public TextPrompter(Shell parent) {
			this(parent, SWT.APPLICATION_MODAL);
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String string) {
			message = string;
		}

		public String open() {
			dialog = new Shell(getParent(), getStyle());
			dialog.setText(getText());
			dialog.setLayout(new GridLayout());
			Label label = new Label(dialog, SWT.NULL);
			label.setText(message);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			text = new Text(dialog, SWT.SINGLE | SWT.BORDER);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.widthHint = 300;
			text.setLayoutData(data);
			Composite buttons = new Composite(dialog, SWT.NONE);
			GridLayout grid = new GridLayout();
			grid.numColumns = 2;
			buttons.setLayout(grid);
			buttons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			Button ok = new Button(buttons, SWT.PUSH);
			ok.setText(ImageAnalyzer.bundle.getString("OK"));
			data = new GridData();
			data.widthHint = 75;
			ok.setLayoutData(data);
			ok.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					result = text.getText();
					dialog.dispose();
				}
			});
			Button cancel = new Button(buttons, SWT.PUSH);
			cancel.setText(ImageAnalyzer.bundle.getString("Cancel"));
			data = new GridData();
			data.widthHint = 75;
			cancel.setLayoutData(data);
			cancel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					dialog.dispose();
				}
			});
			dialog.setDefaultButton(ok);
			dialog.pack();
			dialog.open();
			while (!dialog.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			return result;
		}
	}

	public ImageAnalyzer(Composite parent, int style) {
		super(parent, style);

		display = parent.getDisplay();

		// Hook resize and dispose listeners.
		parent.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				resizeShell(event);
			}
		});
		// parent.addShellListener(new ShellAdapter() {
		// public void shellClosed(ShellEvent e) {
		// animate = false; // stop any animation in progress
		// if (animateThread != null) {
		// // wait for the thread to die before disposing the shell.
		// while (animateThread.isAlive()) {
		// if (!display.readAndDispatch()) display.sleep();
		// }
		// }
		// e.doit = true;
		// }
		// });
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				// Clean up.
				if (image != null) {
					image.dispose();
				}
				whiteColor.dispose();
				blackColor.dispose();
				redColor.dispose();
				greenColor.dispose();
				blueColor.dispose();
				fixedWidthFont.dispose();
				crossCursor.dispose();
			}
		});

		// Create colors and fonts.
		whiteColor = new Color(display, 255, 255, 255);
		blackColor = new Color(display, 0, 0, 0);
		redColor = new Color(display, 255, 0, 0);
		greenColor = new Color(display, 0, 255, 0);
		blueColor = new Color(display, 0, 0, 255);
		fixedWidthFont = new Font(display, "courier", 10, 0);
		crossCursor = new Cursor(display, SWT.CURSOR_CROSS);

		// Add a menu bar and widgets.
		createWidgets();

		// Create a GC for drawing, and hook the listener to dispose it.
		imageCanvasGC = new GC(imageCanvas);
		imageCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				imageCanvasGC.dispose();
			}
		});
	}

	void createWidgets() {

		// create the desired layout for this wizard page
		GridLayout gdCanvas = new GridLayout();
		gdCanvas.numColumns = 1;
		setLayout(gdCanvas);

		// Add a this to contain some control widgets across the top.
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 3;

		Composite top = new Composite(this, SWT.NULL);
		top.setLayout(topLayout);
		top.setLayoutData(new GridData());

		// Canvas to show the image.
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;

		imageCanvas = new Canvas(this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_REDRAW_RESIZE);
		imageCanvas.setBackground(whiteColor);
		imageCanvas.setCursor(crossCursor);
		imageCanvas.setLayoutData(gridData);
		imageCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				if (image != null) {
					paintImage(event);
				}
			}
		});
		imageCanvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent event) {
				if (image != null) {
					showColorAt(event.x, event.y);
				}
			}
		});

		imageCanvas.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent event) {
				if (image != null) {
					displayItemAt(event);
				}
			}

			public void mouseDown(MouseEvent event) {
			}

			public void mouseUp(MouseEvent event) {
			}
		});

		// Set up the image canvas scroll bars.
		ScrollBar horizontal = imageCanvas.getHorizontalBar();
		horizontal.setVisible(true);
		horizontal.setMinimum(0);
		horizontal.setEnabled(false);
		horizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollHorizontally((ScrollBar) event.widget);
			}
		});
		ScrollBar vertical = imageCanvas.getVerticalBar();
		vertical.setVisible(true);
		vertical.setMinimum(0);
		vertical.setEnabled(false);
		vertical.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollVertically((ScrollBar) event.widget);
			}
		});
	}

	void menuComposeAlpha(int alpha_op) {
		if (image == null) {
			return;
		}
		animate = false; // stop any animation in progress
		Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
		this.setCursor(waitCursor);
		imageCanvas.setCursor(waitCursor);
		try {
			if (alpha_op == ImageAnalyzer.ALPHA_CONSTANT) {
				imageData.alpha = alpha;
			}
			else {
				imageData.alpha = -1;
				switch (alpha_op) {
				case ALPHA_X:
					for (int y = 0; y < imageData.height; y++) {
						for (int x = 0; x < imageData.width; x++) {
							imageData.setAlpha(x, y, (x + alpha) % 256);
						}
					}
					break;
				case ALPHA_Y:
					for (int y = 0; y < imageData.height; y++) {
						for (int x = 0; x < imageData.width; x++) {
							imageData.setAlpha(x, y, (y + alpha) % 256);
						}
					}
					break;
				default:
					break;
				}
			}
			displayImage(imageData);
		}
		finally {
			this.setCursor(null);
			imageCanvas.setCursor(crossCursor);
			waitCursor.dispose();
		}
	}

	void menuOpenFile() {
		// Get the user to choose an image file.
		FileDialog fileChooser = new FileDialog(getShell(), SWT.OPEN);
		if (lastPath != null) {
			fileChooser.setFilterPath(lastPath);
		}
		fileChooser.setFilterExtensions(new String[] { "*.bmp; *.gif; *.ico; *.jpg; *.pcx; *.png; *.tif", "*.bmp",
		        "*.gif", "*.ico", "*.jpg", "*.pcx", "*.png", "*.tif" });
		fileChooser.setFilterNames(new String[] {
		        ImageAnalyzer.bundle.getString("All_images") + " (bmp, gif, ico, jpg, pcx, png, tif)", "BMP (*.bmp)",
		        "GIF (*.gif)", "ICO (*.ico)", "JPEG (*.jpg)", "PCX (*.pcx)", "PNG (*.png)", "TIFF (*.tif)" });
		String filename = fileChooser.open();
		lastPath = fileChooser.getFilterPath();
		if (filename == null) {
			return;
		}

		menuOpenFile(filename);
	}

	public void menuOpenFile(String filename) {
		animate = false; // stop any animation in progress
		resetScaleCombos();

		Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
		this.setCursor(waitCursor);
		imageCanvas.setCursor(waitCursor);
		try {
			loader = new ImageLoader();
			if (incremental) {
				// Prepare to handle incremental events.
				loader.addImageLoaderListener(new ImageLoaderListener() {
					public void imageDataLoaded(ImageLoaderEvent event) {
						incrementalDataLoaded(event);
					}
				});
				incrementalThreadStart();
			}
			// Read the new image(s) from the chosen file.
			long startTime = System.currentTimeMillis();
			imageDataArray = loader.load(filename);
			loadTime = System.currentTimeMillis() - startTime;
			if (imageDataArray.length > 0) {
				// Cache the filename.
				currentName = filename;
				fileName = filename;

				// Display the first image in the file.
				imageDataIndex = 0;
				displayImage(imageDataArray[imageDataIndex]);
				resetScrollBars();
			}
		}
		catch (SWTException e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Loading_lc"), filename, e);
		}
		catch (SWTError e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Loading_lc"), filename, e);
		}
		finally {
			this.setCursor(null);
			imageCanvas.setCursor(crossCursor);
			waitCursor.dispose();
		}
	}

	void menuOpenURL() {
		animate = false; // stop any animation in progress
		resetScaleCombos();

		// Get the user to choose an image URL.
		TextPrompter textPrompter = new TextPrompter(getShell(), SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		textPrompter.setText(ImageAnalyzer.bundle.getString("OpenURLDialog"));
		textPrompter.setMessage(ImageAnalyzer.bundle.getString("EnterURL"));
		String urlname = textPrompter.open();
		if (urlname == null) {
			return;
		}

		Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
		this.setCursor(waitCursor);
		imageCanvas.setCursor(waitCursor);
		try {
			URL url = new URL(urlname);
			InputStream stream = url.openStream();
			loader = new ImageLoader();
			if (incremental) {
				// Prepare to handle incremental events.
				loader.addImageLoaderListener(new ImageLoaderListener() {
					public void imageDataLoaded(ImageLoaderEvent event) {
						incrementalDataLoaded(event);
					}
				});
				incrementalThreadStart();
			}
			// Read the new image(s) from the chosen file.
			long startTime = System.currentTimeMillis();
			imageDataArray = loader.load(stream);
			loadTime = System.currentTimeMillis() - startTime;
			if (imageDataArray.length > 0) {
				currentName = urlname;
				fileName = null;

				// Display the first image in the file.
				imageDataIndex = 0;
				displayImage(imageDataArray[imageDataIndex]);
				resetScrollBars();
			}
		}
		catch (Exception e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Loading_lc"), urlname, e);
		}
		finally {
			this.setCursor(null);
			imageCanvas.setCursor(crossCursor);
			waitCursor.dispose();
		}
	}

	/*
	 * Called to start a thread that draws incremental images as they are loaded.
	 */
	void incrementalThreadStart() {
		incrementalEvents = new Vector();
		incrementalThread = new Thread("Incremental") {
			public void run() {
				// Draw the first ImageData increment.
				while (incrementalEvents != null) {
					// Synchronize so we don't try to remove when the vector is null.
					synchronized (ImageAnalyzer.this) {
						if (incrementalEvents != null) {
							if (incrementalEvents.size() > 0) {
								ImageLoaderEvent event = (ImageLoaderEvent) incrementalEvents.remove(0);
								if (image != null) {
									image.dispose();
								}
								image = new Image(display, event.imageData);
								imageData = event.imageData;
								imageCanvasGC.drawImage(image,
								        0,
								        0,
								        imageData.width,
								        imageData.height,
								        imageData.x,
								        imageData.y,
								        imageData.width,
								        imageData.height);
							}
							else {
								Thread.yield();
							}
						}
					}
				}
				display.wake();
			}
		};
		incrementalThread.setDaemon(true);
		incrementalThread.start();
	}

	/*
	 * Called when incremental image data has been loaded, for example, for interlaced GIF/PNG or progressive JPEG.
	 */
	void incrementalDataLoaded(ImageLoaderEvent event) {
		// Synchronize so that we do not try to add while
		// the incremental drawing thread is removing.
		synchronized (this) {
			incrementalEvents.addElement(event);
		}
	}

	void menuSave() {
		if (image == null) {
			return;
		}
		animate = false; // stop any animation in progress

		// If the image file type is unknown, we can't 'Save',
		// so we have to use 'Save As...'.
		if ((imageData.type == SWT.IMAGE_UNDEFINED) || (fileName == null)) {
			menuSaveAs();
			return;
		}

		Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
		this.setCursor(waitCursor);
		imageCanvas.setCursor(waitCursor);
		try {
			// Save the current image to the current file.
			loader.data = new ImageData[] { imageData };
			loader.save(fileName, imageData.type);

		}
		catch (SWTException e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Saving_lc"), fileName, e);
		}
		catch (SWTError e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Saving_lc"), fileName, e);
		}
		finally {
			this.setCursor(null);
			imageCanvas.setCursor(crossCursor);
			waitCursor.dispose();
		}
	}

	void menuSaveAs() {
		if (image == null) {
			return;
		}
		animate = false; // stop any animation in progress

		// Get the user to choose a file name and type to save.
		FileDialog fileChooser = new FileDialog(getShell(), SWT.SAVE);
		fileChooser.setFilterPath(lastPath);
		if (fileName != null) {
			String name = fileName;
			int nameStart = name.lastIndexOf(java.io.File.separatorChar);
			if (nameStart > -1) {
				name = name.substring(nameStart + 1);
			}
			fileChooser.setFileName(name);
		}
		fileChooser.setFilterExtensions(new String[] { "*.bmp", "*.gif", "*.ico", "*.jpg", "*.png" });
		fileChooser.setFilterNames(new String[] { "BMP (*.bmp)", "GIF (*.gif)", "ICO (*.ico)", "JPEG (*.jpg)",
		        "PNG (*.png)" });
		String filename = fileChooser.open();
		lastPath = fileChooser.getFilterPath();
		if (filename == null) {
			return;
		}

		// Figure out what file type the user wants saved.
		// We need to rely on the file extension because FileDialog
		// does not have API for asking what filter type was selected.
		int filetype = ImageAnalyzer.determineFileType(filename);
		if (filetype == SWT.IMAGE_UNDEFINED) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR);
			box.setMessage(ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Unknown_extension"),
			        filename.substring(filename.lastIndexOf('.') + 1)));
			box.open();
			return;
		}

		if (new java.io.File(filename).exists()) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
			box.setMessage(ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Overwrite"), filename));
			if (box.open() == SWT.CANCEL) {
				return;
			}
		}

		Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
		this.setCursor(waitCursor);
		imageCanvas.setCursor(waitCursor);
		try {
			// Save the current image to the specified file.
			loader.data = new ImageData[] { imageData };
			loader.save(filename, filetype);

			// Update the shell title and file type label,
			// and use the new file.
			fileName = filename;

		}
		catch (SWTException e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Saving_lc"), filename, e);
		}
		catch (SWTError e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Saving_lc"), filename, e);
		}
		finally {
			this.setCursor(null);
			imageCanvas.setCursor(crossCursor);
			waitCursor.dispose();
		}
	}

	void menuSaveMaskAs() {
		if ((image == null) || !showMask) {
			return;
		}
		if (imageData.getTransparencyType() == SWT.TRANSPARENCY_NONE) {
			return;
		}
		animate = false; // stop any animation in progress

		// Get the user to choose a file name and type to save.
		FileDialog fileChooser = new FileDialog(getShell(), SWT.SAVE);
		fileChooser.setFilterPath(lastPath);
		if (fileName != null) {
			fileChooser.setFileName(fileName);
		}
		fileChooser.setFilterExtensions(new String[] { "*.bmp", "*.gif", "*.ico", "*.jpg", "*.png" });
		fileChooser.setFilterNames(new String[] { "BMP (*.bmp)", "GIF (*.gif)", "ICO (*.ico)", "JPEG (*.jpg)",
		        "PNG (*.png)" });
		String filename = fileChooser.open();
		lastPath = fileChooser.getFilterPath();
		if (filename == null) {
			return;
		}

		// Figure out what file type the user wants saved.
		// We need to rely on the file extension because FileDialog
		// does not have API for asking what filter type was selected.
		int filetype = ImageAnalyzer.determineFileType(filename);
		if (filetype == SWT.IMAGE_UNDEFINED) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR);
			box.setMessage(ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Unknown_extension"),
			        filename.substring(filename.lastIndexOf('.') + 1)));
			box.open();
			return;
		}

		if (new java.io.File(filename).exists()) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
			box.setMessage(ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Overwrite"), filename));
			if (box.open() == SWT.CANCEL) {
				return;
			}
		}

		Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
		this.setCursor(waitCursor);
		imageCanvas.setCursor(waitCursor);
		try {
			// Save the mask of the current image to the specified file.
			ImageData maskImageData = imageData.getTransparencyMask();
			loader.data = new ImageData[] { maskImageData };
			loader.save(filename, filetype);

		}
		catch (SWTException e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Saving_lc"), filename, e);
		}
		catch (SWTError e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Saving_lc"), filename, e);
		}
		finally {
			this.setCursor(null);
			imageCanvas.setCursor(crossCursor);
			waitCursor.dispose();
		}
	}

	void menuPrint() {
		if (image == null) {
			return;
		}

		try {
			// Ask the user to specify the printer.
			PrintDialog dialog = new PrintDialog(getShell(), SWT.NULL);
			PrinterData printerData = dialog.open();
			if (printerData == null) {
				return;
			}

			Printer printer = new Printer(printerData);
			Point screenDPI = display.getDPI();
			Point printerDPI = printer.getDPI();
			int scaleFactor = printerDPI.x / screenDPI.x;
			Rectangle trim = printer.computeTrim(0, 0, 0, 0);
			if (printer.startJob(currentName)) {
				if (printer.startPage()) {
					GC gc = new GC(printer);
					int transparentPixel = imageData.transparentPixel;
					if ((transparentPixel != -1) && !transparent) {
						imageData.transparentPixel = -1;
					}
					Image printerImage = new Image(printer, imageData);
					gc.drawImage(printerImage, 0, 0, imageData.width, imageData.height, -trim.x, -trim.y, scaleFactor
					        * imageData.width, scaleFactor * imageData.height);
					if ((transparentPixel != -1) && !transparent) {
						imageData.transparentPixel = transparentPixel;
					}
					printerImage.dispose();
					gc.dispose();
					printer.endPage();
				}
				printer.endJob();
			}
			printer.dispose();
		}
		catch (SWTError e) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR);
			box.setMessage(ImageAnalyzer.bundle.getString("Printing_error") + e.getMessage());
			box.open();
		}
	}

	void menuReopen() {
		if (currentName == null) {
			return;
		}
		animate = false; // stop any animation in progress
		resetScrollBars();
		resetScaleCombos();
		Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
		this.setCursor(waitCursor);
		imageCanvas.setCursor(waitCursor);
		try {
			loader = new ImageLoader();
			long startTime = System.currentTimeMillis();
			ImageData[] newImageData;
			if (fileName == null) {
				URL url = new URL(currentName);
				InputStream stream = url.openStream();
				newImageData = loader.load(stream);
			}
			else {
				newImageData = loader.load(fileName);
			}
			loadTime = System.currentTimeMillis() - startTime;
			imageDataIndex = 0;
			displayImage(newImageData[imageDataIndex]);

		}
		catch (Exception e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Reloading_lc"), currentName, e);
		}
		finally {
			this.setCursor(null);
			imageCanvas.setCursor(crossCursor);
			waitCursor.dispose();
		}
	}

	/*
	 * Called when the ScaleX combo selection changes.
	 */
	public void scaleX(float xscale) {
		this.xscale = xscale;
		if (image != null) {
			resizeScrollBars();
			imageCanvas.redraw();
		}
	}

	/*
	 * Called when the ScaleY combo selection changes.
	 */
	public void scaleY(float yscale) {
		this.yscale = yscale;
		if (image != null) {
			resizeScrollBars();
			imageCanvas.redraw();
		}
	}

	public void scaleXY(float xscale, float yscale) {
		this.xscale = xscale;
		this.yscale = yscale;
		if (image != null) {
			resizeScrollBars();
			imageCanvas.redraw();
		}
	}

	/*
	 * Loop through all of the images in a multi-image file and display them one after another.
	 */
	void animateLoop() {
		// Create an off-screen image to draw on, and a GC to draw with.
		// Both are disposed after the animation.
		Image offScreenImage = new Image(display, loader.logicalScreenWidth, loader.logicalScreenHeight);
		GC offScreenImageGC = new GC(offScreenImage);

		try {
			// Use syncExec to get the background color of the imageCanvas.
			display.syncExec(new Runnable() {
				public void run() {
					canvasBackground = imageCanvas.getBackground();
				}
			});

			// Fill the off-screen image with the background color of the canvas.
			offScreenImageGC.setBackground(canvasBackground);
			offScreenImageGC.fillRectangle(0, 0, loader.logicalScreenWidth, loader.logicalScreenHeight);

			// Draw the current image onto the off-screen image.
			offScreenImageGC.drawImage(image,
			        0,
			        0,
			        imageData.width,
			        imageData.height,
			        imageData.x,
			        imageData.y,
			        imageData.width,
			        imageData.height);

			int repeatCount = loader.repeatCount;
			while (animate && ((loader.repeatCount == 0) || (repeatCount > 0))) {
				if (imageData.disposalMethod == SWT.DM_FILL_BACKGROUND) {
					// Fill with the background color before drawing.
					Color bgColor = null;
					int backgroundPixel = loader.backgroundPixel;
					if (showBackground && (backgroundPixel != -1)) {
						// Fill with the background color.
						RGB backgroundRGB = imageData.palette.getRGB(backgroundPixel);
						bgColor = new Color(null, backgroundRGB);
					}
					try {
						offScreenImageGC.setBackground(bgColor != null ? bgColor : canvasBackground);
						offScreenImageGC.fillRectangle(imageData.x, imageData.y, imageData.width, imageData.height);
					}
					finally {
						if (bgColor != null) {
							bgColor.dispose();
						}
					}
				}
				else if (imageData.disposalMethod == SWT.DM_FILL_PREVIOUS) {
					// Restore the previous image before drawing.
					offScreenImageGC.drawImage(image,
					        0,
					        0,
					        imageData.width,
					        imageData.height,
					        imageData.x,
					        imageData.y,
					        imageData.width,
					        imageData.height);
				}

				// Get the next image data.
				imageDataIndex = (imageDataIndex + 1) % imageDataArray.length;
				imageData = imageDataArray[imageDataIndex];
				image.dispose();
				image = new Image(display, imageData);

				// Draw the new image data.
				offScreenImageGC.drawImage(image,
				        0,
				        0,
				        imageData.width,
				        imageData.height,
				        imageData.x,
				        imageData.y,
				        imageData.width,
				        imageData.height);

				// Draw the off-screen image to the screen.
				imageCanvasGC.drawImage(offScreenImage, 0, 0);

				// Sleep for the specified delay time before drawing again.
				try {
					Thread.sleep(ImageAnalyzer.visibleDelay(imageData.delayTime * 10));
				}
				catch (InterruptedException e) {
				}

				// If we have just drawn the last image in the set,
				// then decrement the repeat count.
				if (imageDataIndex == imageDataArray.length - 1) {
					repeatCount--;
				}
			}
		}
		finally {
			offScreenImage.dispose();
			offScreenImageGC.dispose();
		}
	}

	/*
	 * Called when the Previous button is pressed. Display the previous image in a multi-image file.
	 */
	void previous() {
		if ((image != null) && (imageDataArray.length > 1)) {
			if (imageDataIndex == 0) {
				imageDataIndex = imageDataArray.length;
			}
			imageDataIndex = imageDataIndex - 1;
			displayImage(imageDataArray[imageDataIndex]);
		}
	}

	/*
	 * Called when the Next button is pressed. Display the next image in a multi-image file.
	 */
	void next() {
		if ((image != null) && (imageDataArray.length > 1)) {
			imageDataIndex = (imageDataIndex + 1) % imageDataArray.length;
			displayImage(imageDataArray[imageDataIndex]);
		}
	}

	void displayImage(ImageData newImageData) {
		if (incremental && (incrementalThread != null)) {
			// Tell the incremental thread to stop drawing.
			synchronized (this) {
				incrementalEvents = null;
			}

			// Wait until the incremental thread is done.
			while (incrementalThread.isAlive()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}

		// Dispose of the old image, if there was one.
		if (image != null) {
			image.dispose();
		}

		try {
			// Cache the new image and imageData.
			image = new Image(display, newImageData);
			imageData = newImageData;

		}
		catch (SWTException e) {
			showErrorDialog(ImageAnalyzer.bundle.getString("Creating_from") + " ", currentName, e);
			image = null;
			return;
		}

		// Redraw both canvases.
		imageCanvas.redraw();
	}

	void paintImage(PaintEvent event) {
		Image paintImage = image;
		int transparentPixel = imageData.transparentPixel;
		if ((transparentPixel != -1) && !transparent) {
			imageData.transparentPixel = -1;
			paintImage = new Image(display, imageData);
		}
		int w = Math.round(imageData.width * xscale);
		int h = Math.round(imageData.height * yscale);
		event.gc.drawImage(paintImage,
		        0,
		        0,
		        imageData.width,
		        imageData.height,
		        ix + imageData.x,
		        iy + imageData.y,
		        w,
		        h);
		if (showMask && (imageData.getTransparencyType() != SWT.TRANSPARENCY_NONE)) {
			ImageData maskImageData = imageData.getTransparencyMask();
			Image maskImage = new Image(display, maskImageData);
			event.gc.drawImage(maskImage, 0, 0, imageData.width, imageData.height, w + 10 + ix + imageData.x, iy
			        + imageData.y, w, h);
			maskImage.dispose();
		}
		if ((transparentPixel != -1) && !transparent) {
			imageData.transparentPixel = transparentPixel;
			paintImage.dispose();
		}
	}

	void resizeShell(ControlEvent event) {
		if ((image == null) || isDisposed()) {
			return;
		}
		resizeScrollBars();
	}

	// Reset the scale combos to 1.
	public void resetScaleCombos() {
		xscale = 1;
		yscale = 1;
	}

	// Reset the scroll bars to 0.
	void resetScrollBars() {
		if (image == null) {
			return;
		}
		ix = 0;
		iy = 0;
		py = 0;
		resizeScrollBars();
		imageCanvas.getHorizontalBar().setSelection(0);
		imageCanvas.getVerticalBar().setSelection(0);
	}

	void resizeScrollBars() {
		// Set the max and thumb for the image canvas scroll bars.
		ScrollBar horizontal = imageCanvas.getHorizontalBar();
		ScrollBar vertical = imageCanvas.getVerticalBar();
		Rectangle canvasBounds = imageCanvas.getClientArea();
		int width = Math.round(imageData.width * xscale);
		if (width > canvasBounds.width) {
			// The image is wider than the canvas.
			horizontal.setEnabled(true);
			horizontal.setMaximum(width);
			horizontal.setThumb(canvasBounds.width);
			horizontal.setPageIncrement(canvasBounds.width);
		}
		else {
			// The canvas is wider than the image.
			horizontal.setEnabled(false);
			if (ix != 0) {
				// Make sure the image is completely visible.
				ix = 0;
				imageCanvas.redraw();
			}
		}
		int height = Math.round(imageData.height * yscale);
		if (height > canvasBounds.height) {
			// The image is taller than the canvas.
			vertical.setEnabled(true);
			vertical.setMaximum(height);
			vertical.setThumb(canvasBounds.height);
			vertical.setPageIncrement(canvasBounds.height);
		}
		else {
			// The canvas is taller than the image.
			vertical.setEnabled(false);
			if (iy != 0) {
				// Make sure the image is completely visible.
				iy = 0;
				imageCanvas.redraw();
			}
		}
	}

	/*
	 * Called when the image canvas' horizontal scrollbar is selected.
	 */
	void scrollHorizontally(ScrollBar scrollBar) {
		if (image == null) {
			return;
		}
		Rectangle canvasBounds = imageCanvas.getClientArea();
		int width = Math.round(imageData.width * xscale);
		int height = Math.round(imageData.height * yscale);
		if (width > canvasBounds.width) {
			// Only scroll if the image is bigger than the canvas.
			int x = -scrollBar.getSelection();
			if (x + width < canvasBounds.width) {
				// Don't scroll past the end of the image.
				x = canvasBounds.width - width;
			}
			imageCanvas.scroll(x, iy, ix, iy, width, height, false);
			ix = x;
		}
	}

	/*
	 * Called when the image canvas' vertical scrollbar is selected.
	 */
	void scrollVertically(ScrollBar scrollBar) {
		if (image == null) {
			return;
		}
		Rectangle canvasBounds = imageCanvas.getClientArea();
		int width = Math.round(imageData.width * xscale);
		int height = Math.round(imageData.height * yscale);
		if (height > canvasBounds.height) {
			// Only scroll if the image is bigger than the canvas.
			int y = -scrollBar.getSelection();
			if (y + height < canvasBounds.height) {
				// Don't scroll past the end of the image.
				y = canvasBounds.height - height;
			}
			imageCanvas.scroll(ix, y, ix, iy, width, height, false);
			iy = y;
		}
	}

	/*
	 * Return a String containing a line-by-line dump of the data in the current imageData. The lineDelimiter parameter
	 * must be a string of length 1 or 2.
	 */
	String dataHexDump(String lineDelimiter) {
		if (image == null) {
			return "";
		}
		char[] dump = new char[imageData.height * (6 + 3 * imageData.bytesPerLine + lineDelimiter.length())];
		int index = 0;
		for (int i = 0; i < imageData.data.length; i++) {
			if (i % imageData.bytesPerLine == 0) {
				int line = i / imageData.bytesPerLine;
				dump[index++] = Character.forDigit(line / 1000 % 10, 10);
				dump[index++] = Character.forDigit(line / 100 % 10, 10);
				dump[index++] = Character.forDigit(line / 10 % 10, 10);
				dump[index++] = Character.forDigit(line % 10, 10);
				dump[index++] = ':';
				dump[index++] = ' ';
			}
			byte b = imageData.data[i];
			dump[index++] = Character.forDigit((b & 0xF0) >> 4, 16);
			dump[index++] = Character.forDigit(b & 0x0F, 16);
			dump[index++] = ' ';
			if ((i + 1) % imageData.bytesPerLine == 0) {
				dump[index++] = lineDelimiter.charAt(0);
				if (lineDelimiter.length() > 1) {
					dump[index++] = lineDelimiter.charAt(1);
				}
			}
		}
		String result = "";
		try {
			result = new String(dump);
		}
		catch (OutOfMemoryError e) {
			/* Too much data to display in the text widget - truncate at 4M. */
			result = new String(dump, 0, 4 * 1024 * 1024) + "\n ...data dump truncated at 4M...";
		}
		return result;
	}

	/*
	 * Open an error dialog displaying the specified information.
	 */
	void showErrorDialog(String operation, String filename, Throwable e) {
		MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR);
		String message = ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Error"), new String[] { operation,
		        filename });
		String errorMessage = "";
		if (e != null) {
			if (e instanceof SWTException) {
				SWTException swte = (SWTException) e;
				errorMessage = swte.getMessage();
				if (swte.throwable != null) {
					errorMessage += ":\n" + swte.throwable.toString();
				}
			}
			else if (e instanceof SWTError) {
				SWTError swte = (SWTError) e;
				errorMessage = swte.getMessage();
				if (swte.throwable != null) {
					errorMessage += ":\n" + swte.throwable.toString();
				}
			}
			else {
				errorMessage = e.toString();
			}
		}
		box.setMessage(message + errorMessage);
		box.open();
	}

	/*
	 * Return a String describing how to analyze the bytes in the hex dump.
	 */
	static String depthInfo(int depth) {
		Object[] args = { new Integer(depth), "" };
		switch (depth) {
		case 1:
			args[1] = ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Multi_pixels"), new Object[] {
			        new Integer(8), " [01234567]" });
			break;
		case 2:
			args[1] = ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Multi_pixels"), new Object[] {
			        new Integer(4), "[00112233]" });
			break;
		case 4:
			args[1] = ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Multi_pixels"), new Object[] {
			        new Integer(2), "[00001111]" });
			break;
		case 8:
			args[1] = ImageAnalyzer.bundle.getString("One_byte");
			break;
		case 16:
			args[1] = ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Multi_bytes"), new Integer(2));
			break;
		case 24:
			args[1] = ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Multi_bytes"), new Integer(3));
			break;
		case 32:
			args[1] = ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Multi_bytes"), new Integer(4));
			break;
		default:
			args[1] = ImageAnalyzer.bundle.getString("Unsupported_lc");
		}
		return ImageAnalyzer.createMsg(ImageAnalyzer.bundle.getString("Depth_info"), args);
	}

	/*
	 * Return the specified number of milliseconds. If the specified number of milliseconds is too small to see a visual
	 * change, then return a higher number.
	 */
	static int visibleDelay(int ms) {
		if (ms < 20) {
			return ms + 30;
		}
		if (ms < 30) {
			return ms + 10;
		}
		return ms;
	}

	/*
	 * Return the specified byte value as a hex string, preserving leading 0's.
	 */
	static String toHexByteString(int i) {
		if (i <= 0x0f) {
			return "0" + Integer.toHexString(i);
		}
		return Integer.toHexString(i & 0xff);
	}

	/*
	 * Return the specified 4-byte value as a hex string, preserving leading 0's. (a bit 'brute force'... should
	 * probably use a loop...)
	 */
	static String toHex4ByteString(int i) {
		String hex = Integer.toHexString(i);
		if (hex.length() == 1) {
			return "0000000" + hex;
		}
		if (hex.length() == 2) {
			return "000000" + hex;
		}
		if (hex.length() == 3) {
			return "00000" + hex;
		}
		if (hex.length() == 4) {
			return "0000" + hex;
		}
		if (hex.length() == 5) {
			return "000" + hex;
		}
		if (hex.length() == 6) {
			return "00" + hex;
		}
		if (hex.length() == 7) {
			return "0" + hex;
		}
		return hex;
	}

	/*
	 * Return a String describing the specified transparent or background pixel.
	 */
	static String pixelInfo(int pixel) {
		if (pixel == -1) {
			return pixel + " (" + ImageAnalyzer.bundle.getString("None_lc") + ")";
		}
		else {
			return pixel + " (0x" + Integer.toHexString(pixel) + ")";
		}
	}

	/*
	 * Return a String describing the specified disposal method.
	 */
	static String disposalString(int disposalMethod) {
		switch (disposalMethod) {
		case SWT.DM_FILL_NONE:
			return ImageAnalyzer.bundle.getString("None_lc");
		case SWT.DM_FILL_BACKGROUND:
			return ImageAnalyzer.bundle.getString("Background_lc");
		case SWT.DM_FILL_PREVIOUS:
			return ImageAnalyzer.bundle.getString("Previous_lc");
		}
		return ImageAnalyzer.bundle.getString("Unspecified_lc");
	}

	/*
	 * Return a String describing the specified image file type.
	 */
	static String fileTypeString(int filetype) {
		if (filetype == SWT.IMAGE_BMP) {
			return "BMP";
		}
		if (filetype == SWT.IMAGE_GIF) {
			return "GIF";
		}
		if (filetype == SWT.IMAGE_ICO) {
			return "ICO";
		}
		if (filetype == SWT.IMAGE_JPEG) {
			return "JPEG";
		}
		if (filetype == SWT.IMAGE_PNG) {
			return "PNG";
		}
		return ImageAnalyzer.bundle.getString("Unknown_ac");
	}

	/*
	 * Called when the mouse moves in the image canvas. Show the color of the image at the point under the mouse.
	 */
	void displayItemAt(MouseEvent ev) {
		int mx = ev.x;
		int my = ev.y;
		int x = mx - imageData.x - ix;
		int y = my - imageData.y - iy;

		MapArea area = map.getArea(x, y);
		if (area != null) {
			for (Iterator iter = listeners.iterator(); iter.hasNext();) {
				ImageAnalyzerSelectionListener listener = (ImageAnalyzerSelectionListener) iter.next();
				ImageAnalyzerSelectionEvent event = new ImageAnalyzerSelectionEvent(ev);
				event.data = area.anchor;
				listener.select(event);
			}
		}
	}

	/*
	 * Called when the mouse moves in the image canvas. Show the color of the image at the point under the mouse.
	 */
	void showColorAt(int mx, int my) {
		int x = mx - imageData.x - ix;
		int y = my - imageData.y - iy;
	}

	/*
	 * Return the specified file's image type, based on its extension. Note that this is not a very robust way to
	 * determine image type, and it is only to be used in the absence of any better method.
	 */
	static int determineFileType(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.') + 1);
		if (ext.equalsIgnoreCase("bmp")) {
			return SWT.IMAGE_BMP;
		}
		if (ext.equalsIgnoreCase("gif")) {
			return SWT.IMAGE_GIF;
		}
		if (ext.equalsIgnoreCase("ico")) {
			return SWT.IMAGE_ICO;
		}
		if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
			return SWT.IMAGE_JPEG;
		}
		if (ext.equalsIgnoreCase("png")) {
			return SWT.IMAGE_PNG;
		}
		return SWT.IMAGE_UNDEFINED;
	}

	static String createMsg(String msg, Object[] args) {
		MessageFormat formatter = new MessageFormat(msg);
		return formatter.format(args);
	}

	static String createMsg(String msg, Object arg) {
		MessageFormat formatter = new MessageFormat(msg);
		return formatter.format(new Object[] { arg });
	}

	public void load(String cmapImageFileName) {
		map.build(cmapImageFileName);
	}
}
