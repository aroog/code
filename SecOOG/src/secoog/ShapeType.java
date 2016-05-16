package secoog;

// TODO: Why no HighLevelProcess? Why no Boundary?
public enum ShapeType {
	Process, DataStore, ExternalInteractor, Unknown};


// TODO: This is not very O-O.
// Why don't we have subclasses, for DataStore? Or ExternalInteractor?
// It's because our analysis does not create different types of SecObjects