Architectural Reasoning based on Ownership Object Graphs (OOGs)
===============================================================

Main applications of OOGs:
--------------------------
* Security Constraints On RuntIme Architecture (Scoria): reasoning about system security based on abstractions of the runtime architecture;
* Static Conformance cHecking of Object-based structuraL vIews of Architecture (SCHOLIA): analyzing conformance to target architecture;
* Program Comprehension: code exploration and navigation focusing on the abstract runtime structure;
* Metrics: compute metrics on a corpus of code based on a hierarchical object graph;
* Impact analysis: perform more precise impact analysis based on a hierarchical object graph;
* Interactive Refinement of Hierarchical Object Graphs: infer ownership type qualifiers based on the interactive manipulation or refinement of a hierarchical object graph;

List of projects:
-----------------
* Simple.XML: Eclipse-based wrapper for simple.xml framework
* MiniAstOOG: Simple Abstract Syntax Tree (AST) representation
* SecOOG: Scoria security analysis
* PointsTo: ScoriaX object graph extractor (supports Points-To, Data-flow and Control-flow edges)
* OOGEclipsePlugin: serve the results of the Eclipse analysis as JSON objects in a web-browser
* OOGWeb: JavaScript based web client
* ArchDoc: the Eclipse Runtime Perspective for code exploration and program comprehension
* ArchMetrics: 
* ArchSummary: impact analysis based on a hierarchical object graph
* OOGRE: Object Graph REfinement (OOGRE)

List of contributors:
---------------------

* Marwan Abi-Antoun
* Radu Vanciu: PointsToOOG, SecOOG, MiniAstOOG
* Ebrahim Khalaj: OOGRE
* Andrew Giang: ArchDoc, MiniAstOOG, ArchTrace, ArchSummary, ArchMetrics
* Ahmad Moghimi: OOGWeb, OOGEclipsePlugin
* Yibin Wang: ArchSummary
* Sumukhi Chandrashekar: ArchMetrics
