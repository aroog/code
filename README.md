Architectural Reasoning based on Ownership Object Graphs (OOGs)
===============================================================

Main applications of OOGs:
--------------------------
* Security Constraints On RuntIme Architecture (Scoria): reasoning about system security based on abstractions of the runtime architecture;
* Static Conformance cHecking of Object-based structuraL vIews of Architecture (SCHOLIA): analyzing conformance to target architecture;
* Program Comprehension: code exploration and navigation focusing on the abstract runtime structure;
* Metrics: compute metrics on a corpus of code based on a hierarchical object graph; use R to generate various statistics;
* Impact analysis: perform more precise impact analysis based on a hierarchical object graph;
* Interactive Refinement of Hierarchical Object Graphs: infer ownership type qualifiers based on the interactive manipulation or refinement of a hierarchical object graph;

List of key projects:
---------------------
* SecOOG: Scoria security analysis
* PointsTo: ScoriaX object graph extractor (supports Points-To, Data-flow and Control-flow edges)
* OOGEclipsePlugin: serve the results of the Eclipse analysis as JSON objects in a web-browser
* OOGWeb: JavaScript based web client
* ArchDoc: the Eclipse Runtime Perspective for code exploration and program comprehension
* ArchMetrics: compute metrics based on an abstract object graph
* ArchSummary: impact analysis based on a hierarchical object graph
* OOGRE: Object Graph REfinement (OOGRE)
* OOGRE2: version 2.0: supports Auto and Assisted mode.

Utility/Helper projects:
* Simple.XML: Eclipse-based wrapper for simple.xml framework
* MiniAstOOG: Simple Abstract Syntax Tree (AST) representation
* ArchTrace:
* ArchLib:
* ArchViewer: nested box visualization for OOGs using GraphViz DOT; uses ZGRViewer;
* ArchViewerWeb (Lite): nested box visualization for OOGs using GraphViz DOT; optimized for the web; uses Scalable Vector Graphics (SVG);
* MOOG: mother OOG facade to share the same OOG across tool stack
* MOOGREX: integrate refinement and extraction


List of contributors:
---------------------

* Marwan Abi-Antoun
* Radu Vanciu: PointsToOOG, SecOOG, MiniAstOOG, ArchMetrics
* Ebrahim Khalaj: OOGRE, OOGRE2
* Andrew Giang: ArchDoc, MiniAstOOG, ArchTrace, ArchSummary, ArchMetrics
* Ahmad Moghimi: OOGWeb, OOGEclipsePlugin
* Yibin Wang: logging functionality in ArchSummary
* Sumukhi Chandrashekar: some contributions to ArchMetrics


Credits:
--------
* GraphViz DOT
* ZGRViewer

