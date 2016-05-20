webApp.controller('treeController', function ($scope, $interval, $rootScope, $routeParams, $route, $sce, API) {
    $scope.loading = false;
    $scope.pendingAddRefine = false;
    $interval.cancel($rootScope.pluginListTimer);
    
   	JSONEditor.defaults.theme = 'bootstrap3';
	JSONEditor.defaults.options.iconlib = "bootstrap3";
	  $scope.editorLoaded = function(jsonEditor){
        $scope.jsonEditor=jsonEditor;
  	   };

     var defaultGraph = {
        properties: {
            showPtEdges: true,
            showDfEdges: true,
            showCrEdges: true,
            showEdgeLabels: true
        }
     };

     var defaultAnalysis = {
        properties: {
            runOOGRE: true,
            runPointsTo: true
        }
     };

     $scope.defaultSettings = {};
     $scope.defaultSettings["graph"] = defaultGraph;
     $scope.defaultSettings["analysis"] = defaultAnalysis;  

     var graphSchema = {
       format: "list",
       type: "object",
       title: "Graph Settings",
       properties: {
            settingsKey: {
                title: "settings",
                type: "string",
                template: "graph"
            },
            properties: {
                   type: "object",
                   title: "properties",
                   properties: {
                 showPtEdges: {
                   type: "boolean",
                   title: "Show points-to edges",
                   "format": "checkbox"
                 },         
                 showDfEdges: {
                   type: "boolean",
                   title: "Show dataflow edges",
                   "format": "checkbox"
                 },
                 showCrEdges: {
                   type: "boolean",
                   title: "Show creational edges",
                   "format": "checkbox"
                 },
                 showEdgeLabels: {
                   type: "boolean",
                   title: "Show edge labels",
                   "format": "checkbox"
                 }
             }
            }
        }        
     };
     var analysisSchema = {
        format: "list",
        type: "object",
        title: "Analysis Settings",
        properties: {
            settingsKey: {
                title: "settings",
                type: "string",
                template: "analysis"
            },
            properties: {
                   type: "object",
                   title: "properties",
                   properties: {
                     runPointsTo:{
                        type: "boolean",
                        title: "Extract OOG (if unchecked, will load from file)",
                        format: "checkbox"
                     },
                     runOOGRE: {
                       type: "boolean",
                       title: "Infer annotations",
                       "format": "checkbox"
                     }
                 }
             }
        }
     };

    var defaultOObject = {
       text: "dummy",
       objectType: "DUMMY",
       properties: {
         isConfidential: false,
         isSanitized: null,
         trustLevel: 'Unknown',
         visible: true,
         showInternal: true,
       }
     };

     var oobjectSchema = {
           format: "grid",
           type: "object",
           title: "OObject",
           properties: {
             text: {
                type: "string"
             },
             objectKey: {
                type: "string"
             },
             properties: {
               type: "object",
               title: "properties",
               properties: {
                 isConfidential: {
                   type: "boolean",
                   "format": "checkbox"
                 },
                 isSanitized: {
                   type: "string",
                   enum: ["Unknown","True", "False"]
                 },
                 trustLevel: {
                   type: "string",
                   enum: ["Unknown","Trusted", "Untrusted"]
                 },
                 visible: {
                   type: "boolean",
                   "format": "checkbox"
                 },
                 showInternal: {
                   "type": "boolean",
                   "format": "checkbox"
                 }
               }
             }
           }
         };

     var odomainSchema = {
       format: "grid",
       type: "object",
       title: "ODomain",
       properties: {
         text: {
            type: "string"
         }
        }        
     };
    
    $scope.schemas = {}; 
    $scope.schemas["graph"] = graphSchema;    
    $scope.schemas["object"] = oobjectSchema;
    $scope.schemas["domain"] = odomainSchema;
    $scope.schemas["analysis"] = analysisSchema;
     $scope.obj = {
       options: {
         change: function(value) {
           console.log(value);
         },
         startval: defaultGraph,
         ajax: true,
         disable_array_add: true,
         disable_edit_json: true,
         no_additional_properties: true,
         disable_array_delete: true,
         disable_array_reorder: true,
         disable_properties: true,
         schema: graphSchema 
       }
     };

    /*
     * Content Menu object definition for SVG Graph
     */
    $scope.graphMenu = contextmenu([
        {
            /*
             *  Zoom In Action
             */
            label: "Zoom In",
            onclick: function (e) {
                $scope.zoomIn();
            }
        },
        {
            /*
             *  Zoom out Action
             */
            label: "Zoom out",
            onclick: function (e) {
                $scope.zoomOut();
            }
        },
        {
            /*
             *  Open the latest save encoded SVG data with data: protocol in
             *   new view. So The user is able to save SVG file.
             */
            label: "Open in new ...",
            onclick: function (e) {
                var svg = angular.element('#' + $scope.currentGraphId).html();

                // Restore the XML header for SVG by clearing out the comment
                svg = svg.replace('<!--', '');
                window.open('data:image/svg+xml;base64,' + btoa(svg), 'Download');
            }
        }
    ]);

    /*
     * Check if there is a Pending refinement in refinements list
     */
    $scope.isPendingInList = function () {
        if (angular.isDefined($scope.refinements)) {
            for (var k in $scope.refinements) {
                if ($scope.refinements[k].state == 'Pending')
                    return true;
            }
        }
        return false;
    }

    /*
     * Set the current graph Id, so the Graph Context Menu and Zoom In/Out
     * button know the target Graph to manipulate.
     * Called by switching between panes "Display Graph" and "Object Graph"
     */
    $scope.setCurrentGraph = function (id) {
        $scope.currentGraphId = id;
    }

    /*
     * Content Menu object definition for the Refinement List
     */
    $scope.createRefineMenu = function () {
        var menues = [
            {
                label: "Export as JSON",
                onclick: function (e, source) {
                    var formattedJSON = JSON.stringify($scope.refinements, null, 2);
                    $scope.download('application/json', 'export_refinement.json', formattedJSON);
                }
            },
            {
                label: "Export as CSV",
                onclick: function (e, source) {
                    var csv = $scope.createCSVfromRefinements($scope.refinements);
                    $scope.download('text/csv', 'export_refinement.csv', csv);
                }
            }
        ];

        if ($scope.isPendingInList()) {
            menues.push({
                label: "Remove Pending",
                onclick: function (e, source) {
                    if (!$scope.pendingAddRefine && !$scope.refinementProcessing)
                        $scope.triggerRemoveRefinement();
                }
            });
        }

        return contextmenu(menues);
    }

    /*
     * This function add an html <a> tag with data protocol containes something like:
     *   <a display:hidden href="data:csvType>datada</a>
     *  And then do the click for the tag to simulate a download
     *   <a>.click();
     */
    $scope.download = function (mimeType, fileName, data) {
        var id = fileName.replace(/\./g, '__');
        var href = 'data:"' + mimeType + ';charset=utf-8,' + encodeURIComponent(data) + '"';
        if (angular.element('#' + id).length) {
            angular.element('#' + id).attr('href', href);
        } else {
            angular.element("body").append(
                '<a style="display:none" id="' +
                id +
                '" href=' +
                href +
                ' download="' +
                fileName +
                '"></a>'
            );
        }
        document.getElementById(id).click();
    }


    $scope.createCSVfromRefinements = function (refinements) {
        var csv = 'Type,ID,Source,Destination,Domain,State\r\n';
        refinements.forEach(function (refinement) {
            var line = '';
            line += refinement.type + ',';
            line += refinement.refId + ',';
            line += refinement.srcObject + ',';
            line += refinement.dstObject + ',';
            line += refinement.dstDomain + ',';
            line += refinement.state;
            csv += line;
            csv += '\r\n';
        });
        return csv;
    }

    /*
     * JSTree actions & functionalities
     */

    // Store targeted element with tag ID='jstree'
    $scope.t = angular.element('#jstree');

    /*
     * Check if a Drag&Drop action is allowed or not
     *  - if source or destination is undefined return false 
     *  - If both source & destination type are same. Ex: Both of them are domain or object
     *     return false
     *  - If source is domain (regardless whether destination is object) return false
     *  - If source is expressions (expression container) just return false
     *  - If source is expression and destination is another object return false
     *  - If source is expression and destination is another expressions return false
     * CAREFUL: jstree: has its own representation of the node.
     * So we have to use node.original to get the actual node object without any modifications by jstree
     * Params: 
     *   node: source node,
     *   node_parent: destination node 
     */
    $scope.t.isAllowed = function (node, node_parent) {
        if (angular.isUndefined(node.original) ||
            angular.isUndefined(node_parent.original))
            return false;

        if (node.original.type === node_parent.original.type)
            return false;

        // Cannot drag-n-drop a domain...
        if (node.original.type === 'domain' /* && node_parent.original.type === 'object' */ )
            return false;

        if (node.original.type === 'expressions')
            return false;

        if (node.original.type === 'expression' && node_parent.original.type === 'object')
            return false;

        if (node.original.type === 'expression' && node_parent.original.type === 'expressions')
            return false;

        return true;
    }

    /*
     * Highlight a node in the current SVG Graph.
     * Double-click on tree selects in the Graph; also context-menu "Find in Graph"
     * Works for both objects and domains.
     * TODO: Extend this to handle showing edges in the tree or selecting edges in the graph
     */
    $scope.highlightGraphNode = function (node) {

        if (node.type === 'objectRef') {
            node = node.ref;
        }

        if (node.objectKey || node.type === "domain") {
            $scope.loadPropertiesFromOState(node);
			$scope.obj.options.schema = $scope.schemas[node.type];
            $scope.obj.options.startval=node;
		} else if (node.type === "settings") {
            $scope.loadPropertiesFromOState(node);
            $scope.obj.options.schema = $scope.schemas[node.id];
            node.settingsKey = node.id;
            $scope.obj.options.startval=node;
        } 


        if($scope.selectMode) {
            $scope.selectNode(node.id);
        }
        /*
         * Node ID in SVG and Tree are different. This helper functin change node id
            from a tree to the format of node id in SVG.
         */
        var convertToSVGFormat = function (id, type) {
            var t = id.replace(/::/g, '__');
            t = t.replace(/\./g, '_');
            if (type === 'domain') {
                t = 'cluster_' + t;
            }
            return t
        }

        // Convert node ID
        var id = convertToSVGFormat(node.id, node.type);

        // Find the SVG <G> tag with the appropriate ID using Jquery
        var target = angular.element('svg').find('*')
            .filter(function () {
                return $(this).text() === id;
            })
            .closest("g");
            
        $scope.$digest();

    }

    /*
     * JSTree initialization config
     */
    var jstreeConfig = {
        core: {

            /*
             * Callback function that get executed on each node change operation
             *   on Tree
             * Important params:
             *   node: targeted node
             *   node_parent: parent of the targeted node
             */
            check_callback: function (operation, node, node_parent, node_position, more) {
                // Clear the refine move scope variable
                $scope.refineMove = {};

                // If the tree is already populated, Go check if the node change is valid or not
                if ($scope.populated) {

                    // If there is a pendingAddRefine operation or a refinement under server-side
                    // processing, don't let the change happen.
                    // In other words, don't allow Drag&Drop
                    if ($scope.pendingAddRefine || $scope.refinementProcessing) {
                        return false;
                    }

                    // From here isAllowed function judge if node movement is credible
                    if ($scope.t.isAllowed(node, node_parent)) {
                        $scope.refineMove.src = node.original;
                        $scope.refineMove.dst = node_parent.original;
                        return true;
                    }
                    return false;
                }
                // otherwise: just let the nodes to change (Tree need to be populated)
                else {
                    return true;
                }

            }
        },

        /*
         * List of imported JStree plugins: 
         *   dnd: Drag&Drop Plugin
         *   types: Node type plugin. Allow metadata info and icon on nodes
         *   contextmenu: Built-in Context menu plugin for tree
         */
        plugins: ['dnd', 'types', 'contextmenu'],

        /*
         * Context menu configuration for Tree
         */
        contextmenu: {
            items: function (node) {
                // Map of context available menus for Tree
                var contextMenus = {

                    // Genral context menu to find a node on graph
                    findInGraph: {
                        label: 'Find in graph',

                        // Call highlightGraphNode to highlight targeted node
                        action: function (obj) {
                            $scope.highlightGraphNode(node.original);
                        }
                    }
                }

                // If the node type is object, Add more options for menu
                if (node.original.type === 'object') {

                    // Populate current trace list to many traces of an object
                    contextMenus.trace = {
                        label: 'Trace to code',
                        action: function (obj) {
                            $scope.currentTraces = node.original.trace;
                            $scope.$digest();

                            // By default show the first trace
                            if (angular.isArray($scope.currentTraces) && $scope.currentTraces.length > 0) {
                                $scope.triggerShowCode($scope.currentTraces[0]);
                            }
                        }
                    };

                }

                //                Disabled due to this Exception: Uncaught TypeError: Converting circular structure to JSON
                //                contextMenus.export = {
                //                    label: 'Export as JSON',
                //                    
                //                    action: function (obj) {
                //                        console.log($scope.OGraph);
                //                        var formattedJSON = JSON.stringify($scope.OGraph, null, 2);
                //                        $scope.download('application/json', 'export_oog.json', formattedJSON);
                //                    }
                //                };

                return contextMenus;
            }
        }
    };

    /**
     * Load the values for the properties, based on the objectKey associated with an Object
     * Either use  default values/
     * Or load from the OGraphState JSON object
     */
    $scope.loadPropertiesFromOState = function (node) {
        var states = $scope.OStates.map;
        if (node.type === 'settings') {
            var targetState = states[node.id];
            node.properties = (angular.isDefined(targetState)) ? targetState.properties: $scope.defaultSettings[node.id].properties; 
        }
        else {
            var targetState = states[node.objectKey];
            if (angular.isDefined(targetState)) {
                node.properties = targetState.properties;
            }
            else {
                node.properties = defaultOObject.properties;
            }
        }
    }

    /*
     * Create objects from the Properties window.
     * Save them back to the OGraphState JSON object being sent to the server
     */
    $scope.savePropertiesToOState = function (node) {
        var props = node.properties;
        if (angular.isDefined(props)) {
            if (node.objectKey) {
                $scope.OStates.map[node.objectKey] = {};
                $scope.OStates.map[node.objectKey].properties = props;
            } else { 
                if (node.settingsKey){
                    $scope.OStates.map[node.settingsKey] = {};
                    $scope.OStates.map[node.settingsKey].properties = props;   
                }
            }
        }
    }

    /*
     * Callback from the UI to save properties on selected object (node)
     */
    $scope.saveProperties = function (node) {
        $scope.savePropertiesToOState(node);
        $scope.triggerSaveStates($scope.OStates);
    }
    

    /**
     * Double-click on node in the tree selects it in the graph.
     */
    $scope.t.jstree(jstreeConfig).bind("dblclick.jstree", function (e) {
        var element = angular.element(e.target).closest("li").get(0);        
        var node = $scope.t.jstree(true).get_node(element.id);
        $scope.highlightGraphNode(node.original);
    });;

    $scope.objects = []; // all the objects in the tree, stored in an array
    $scope.domains = []; // all the domains in the tree, stored in an array
    $scope.miscs = []; // other nodes in the tree (containers, expression, etc.) stored in an array
    $scope.secProperties = [];

    // Use to generate unique IDs for the expression
    $scope.t.addCounter = 0;

    /*
     * Function to add a node to the tree.
     * The node can be an actual (object, domain, expression).
     * Or a container ('expressions').
     */
    $scope.t.addNode = function (parent, node, type) {
        var icon = "jstree-file";
        if (type === 'domain') {
            icon = "jstree-folder";
        }
        var treeNode;
        if (type === 'settings') {
            treeNode = {
                id: node.id,
                text: type + ':' + node.text,
                type: type,
                icon: "glyphicon glyphicon-cog"
            }
        } else if (type === 'properties') {
            treeNode = {
                id: node.id,
                text: node.id,
                type: type,
                icon: "glyphicon glyphicon-tag"
            }
        } else if (type === 'objectRef') {
            treeNode = {
                id: node.id,
                ref: node.ref,
                text: node.ref.text,
                type: type,
                icon: node.ref.icon
            }
        } else if (type === 'expressions') {
            treeNode = {
                id: node.id,
                text: node.text,
                type: type,
                icon: "glyphicon glyphicon-open-file"
            }
        } else if (type === 'expression') {
            treeNode = {
                id: parent + node.expression + $scope.t.addCounter,
                parentObjectId: node.parentObject.o_id,
                objectType: node.parentObject.typeDisplayName,
                text: node.expression,
                type: type,
                icon: "?",
                name: node.name,
                exprKind: node.kind,
                exprType: node.type,
                enclMeth: node.enclosingMethod,
                enclType: node.enclosingType
            }
        } else { // object or domain.
            // XXX. Hackish code that handles both object or domain.
            // XXX. Careful: domain does not have an objectKey.
            treeNode = {
                id: node.d_id ? node.d_id : node.o_id,
                text: node.d ? node.d : node.instanceDisplayName,
                trace: node.traceability2,
                objectKey: node.objectKey,
                objectType: node.typeDisplayName,
                type: type,
                icon: icon
            }
        }


        if (type === 'domain') {
            $scope.domains.push(treeNode);
        } else if (type === 'object') {
            $scope.objects.push(treeNode);
        } else if (type === 'properties') {
            $scope.secProperties.push(treeNode);
        } else {
            $scope.miscs.push(treeNode);
        }

        $scope.t.addCounter++;
        // Insert the node into the tree as the last node
        $scope.t.jstree().create_node('#' + parent, treeNode, "last");
    };

    /*
     * Clear the tree before reloading.
     * Uses trick specific to jstree!
     */
    $scope.t.clearNodes = function () {
        $scope.t.jstree().delete_node($scope.t.jstree().get_json());
    }

    /*
     * Reload the tree when the graph changes
     */
    $scope.t.reloadTree = function () {
        $scope.populated = false;
        $scope.t.clearNodes();
        $scope.t.addNode('',{id:"analysis", text:'analysis'},'settings');
        $scope.t.addNode('',{id:"graph", text:'graph'},'settings');
        $scope.populateTree('', $scope.OGraph);

        // Expand all nodes on load automatically
        $scope.t.jstree('open_all');

        // Collapse all nodes with 'expressions' type
        $scope.miscs.forEach(function (node) {
            if (node.type === 'expressions') {
                $scope.t.jstree("close_node", "#" + node.id);
            }
        })
        $scope.populateProperties($scope.OStates.map);
        $scope.populated = true;
    }

    var reverseProps = function(map){
      var rMap = [];
      angular.forEach(map, function(value, key) {

        if (key != 'analysis' && key != 'graph') {
          angular.forEach(value.properties, function(pvalue, pkey) {
            var arr = {};
            arr.objectKey = key;
            arr.propertyId = pkey + "." + pvalue;
            rMap.push(arr);
          });
        }

      });
      return _.groupBy(_.sortBy(rMap, 'propertyId'),function(arr){return arr.propertyId});
    }

    $scope.populateProperties = function(map) {
        var rMap = reverseProps(map);
        angular.forEach(rMap, function(obj, key){
            $scope.t.addNode('',{id:key},'properties');
            angular.forEach(obj, function(v){
                var oobject = _.find($scope.objects, function(o){return o.objectKey === v.objectKey});
                if (oobject) {
                 $scope.t.addNode(key,{id: key+oobject.id, ref: oobject},'objectRef');
                }
            });    
        });
    }


    $scope.isObjectNode = function (element) {
        return element.hasOwnProperty('o_id');
    };

    /*
     * Cancel the timer and disable the "loading" interface in the CSS
     */
    $scope.cancelCheck = function (timer) {
        $interval.cancel(timer);
        $scope.loading = false;
    }

    // Keep track of visited d_ids, since the OGraph may have cycles.
    var visited = [];
    
    /*
     * Recursive function that traverses the graph and populates the tree.
     * First call: the node is the root object (dummy) and the parent is empty.
     */
    $scope.populateTree = function (parent, node) {
        $scope.populated = false;
        if ($scope.isObjectNode(node)) {
            $scope.t.addNode(parent, node, 'object');
            if (angular.isDefined(node.expressions) && node.expressions.length > 0) {
                var containerId = node.o_id + 'expressions';
                $scope.t.addNode(node.o_id, {
                    id: containerId,
                    text: '[expressions]'
                }, 'expressions');

                node.expressions.forEach(function (expr) {
                    expr.parentObject = node;
                    $scope.t.addNode(containerId, expr, 'expression');
                });
            }
            if (angular.isUndefined(node.children))
                return;
            var IDomain = node.children;
            if (angular.isArray(IDomain)) {
                IDomain.forEach(function (domain) {
                    $scope.populateTree(node.o_id, domain);
                })
            } else {
                $scope.populateTree(node.o_id, IDomain);
            }

        } else {
            $scope.t.addNode(parent, node, 'domain');
            if (angular.isUndefined(node.children))
                return;
            var IObject = node.children;
            if (angular.isArray(IObject)) {
                IObject.forEach(function (obj) {
                    $scope.populateTree(node.d_id, obj);
                })
            } else {
                // If we have not visited this d_id yet, recurse
                if(!angular.isDefined(visited[node.d_id])) {
                    visited.push(node.d_id);
                    $scope.populateTree(node.d_id, IObject);
                }
                
            }
        }
    }

    /*
     * General format of calling server job is:
     * - addJob
     * - return
     * - create a new timer
     * - that timer is responsible for calling checkJob
     * - after we get status 3 (success)
     * - we cancel the check
     * 
     * JOB: GEN_GRAPH
     */
    $scope.triggerPopulateTree = function () {
        $scope.loading = true;
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "GEN_GRAPH"
        }).then(function (data) {
            $rootScope.treeTimer = $interval(function () {
                    API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                        var job = data.job;
                        // Status == 3 => job is done
                        if (angular.isDefined(job) && job.status === 3) {
                            // job.result.success indicates success or failure
                            if (angular.isDefined(job.result.success) && job.result.success == false) {
                                // Do nothing for failure
                                // XXX. We need to add something here.
                                // Display message box, or notification, etc.
                            } else {
                                $scope.OGraph = job.result.root;
                                $scope.t.reloadTree();
                            }
                            $scope.cancelCheck($rootScope.treeTimer);
                        }
                    })
                }, 1000) // Check every 1s if the job is done
        });
    };

    /*
     * JOB: ADD_REFINEMENT
     */
    $scope.triggerAddRefinement = function (params) {
        $scope.loading = true;
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "ADD_REFINEMENT",
            params: params
        }).then(function (data) {
            $rootScope.treeTimer = $interval(function () {
                API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                    var job = data.job;
                    if (angular.isDefined(job) && job.status === 3) {
                        if (angular.isDefined(job.result.success) && job.result.success == false) {
                            // Do nothing
                        } else {
                            $scope.triggerShowRefinementTable();
                        }
                        $scope.cancelCheck($rootScope.treeTimer);
                    }
                })
            }, 1000)
        });
    }

    /*
     * JOB: REMOVE_REFINEMENT.
     * NOTE: can be used to remove only a pending refinement.
     */
    $scope.triggerRemoveRefinement = function () {
        $scope.loading = true;
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "REMOVE_REFINEMENT",
        }).then(function (data) {
            $rootScope.treeTimer = $interval(function () {
                API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                    var job = data.job;
                    if (angular.isDefined(job) && job.status === 3) {
                        if (angular.isDefined(job.result.success) && job.result.success == false) {
                            // Do nothing
                        } else {
                            $scope.triggerShowRefinementTable();
                            $scope.triggerPopulateTree();
                        }
                        $scope.cancelCheck($rootScope.treeTimer);
                    }
                })
            }, 1000)
        });
    }

    /*
     * Trace to code: by selecting and scrolling to line number in the currently open Java file in the Prism component
     */
    $scope.markLineNumber = function (num) {
        angular.element('pre').attr('data-line', num)
            // Show the select line
        Prism.highlightAll();

        // Scroll to the right line
        var highlighted = angular.element('.line-highlight').get(0);
        if (angular.isDefined(highlighted))
            highlighted.scrollIntoView();

    }

    /*
     * JOB: SHOW_CODE.
     * params is a trace object (resource and line)
     */
    $scope.triggerShowCode = function (params) {
        var line = params.line;
        // resource, line
        $scope.loading = true;
        angular.element('#sourceCode').text('');
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "SHOW_CODE",
            params: params
        }).then(function (data) {
            $rootScope.codeTimer = $interval(function () {
                API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                    var job = data.job;
                    if (angular.isDefined(job) && job.status === 3) {
                        if (angular.isDefined(job.result.success) && job.result.success == false) {
                            // Do nothing
                        } else {
                            // atob: because server will encode Java code into Base64.
                            angular.element('#sourceCode').text(atob(job.result));
                            if (line && line !== 0) {
                                // Automatically select the correct line number
                                $scope.markLineNumber(line);
                            } else {
                                $scope.markLineNumber(1);
                            }
                        }
                        $scope.cancelCheck($rootScope.codeTimer);
                    }
                })
            }, 1000)
        });
    }

    //NOTE: This might be changed based on SVG
    //HACK: Connect ID in SVG to ID in the tree
    //Underline vs. Dot. Make them the same. Then we can get rid of extra string manipulation.
    $scope.findIdOfClickedNode = function (container, target) {
        var t = angular.element('#' + container + '> svg > g > #' + target.id + ' > title')
        t = t.html();
        t = t.replace(/cluster_/g, '');
        t = t.replace(/__/g, '::');
        t = t.replace(/_/g, '.');
        return t
    }

    /*
     * JOB: SHOW_GRAPH.
     * Display the OOG (nested boxes)
     */
    $scope.triggerShowGraph = function (params) {
        $scope.loading = true;
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "SHOW_GRAPH",
            params: params
        }).then(function (data) {
            $rootScope.graphTimer = $interval(function () {
                API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                    var job = data.job;
                    if (angular.isDefined(job) && job.status === 3) {
                        if (angular.isDefined(job.result.success) && job.result.success == false) {
                            // Do nothing
                        } else {
                            // HACK: comment out the header being added to the SVG by DOT
                            angular.element('#graphContainer').html('<!--' + atob(job.result));

                            // HACK: remove xlink redirection in frontend using jquery
                            angular.element('g > a').click(function (e) {
                                e.preventDefault();
                            });

                            // HACK: needed to give focus to the SVG graph
                            angular.element("text").focus(function () {});

                            angular.element('svg').each(function (index, element) {
                                contextmenu.attach(element, $scope.graphMenu);
                            });
                            // JQuery to select nodes and clusters in the SVG
                            // and install event handler for the click of each one of them
                            angular.element('#graphContainer > svg > g > .node, #graphContainer > svg > g > .cluster')
                                .click(function (e) {
                                    var id = $scope.findIdOfClickedNode('graphContainer', e.currentTarget);
                                    $scope.selectNode(id);
                                })
                        }
                        $scope.cancelCheck($rootScope.graphTimer);
                    };
                })
            }, 1000)
        });
    }

    /* 
      Toggle between selectMode and regular mode. Changes the mouse cursor.
    */
    $scope.toggleSelectMode = function () {
        $scope.selectMode = !$scope.selectMode;
        if ($scope.selectMode) {
            // Change the mouse cursor
            angular.element('#graphContainer2').css('cursor', 'cell');

        } else {
            angular.element('#graphContainer2').css('cursor', 'default');
            $scope.adjustSecParams();
        }
    }

    $scope.exportSecQueries = function () {
        //        console.log($scope.secQueriesHistory);
        var formattedJSON = JSON.stringify($scope.secQueriesHistory, null, 2);
        $scope.download('application/json', 'export_security_query.json', formattedJSON);
    }


    $scope.selectedSecurity = {
        nodes: [],
        type: 'EdgesBetween'
    };


    $scope.getSecAnalyzeTypeLimit = function () {
        var type = $scope.selectedSecurity.type;
        var ANALYZE_PARAM_LIMIT = {
            Provenance: 4,
            Transitivity: 3,
            Hierarchy: 1,
            Reachability: 1,
            IndirectComm: 2,
            EdgesBetween: 2,
            InformationDisclosure: 0,
            Tampering: 0,
            CustomProperties: 2
        }
        return ANALYZE_PARAM_LIMIT[type];
    }

    /*
     * Create the placeholders, based on the expected number of things to select
     */
    $scope.adjustSecParams = function () {
        $scope.selectedSecurity.nodes = [];
        for (var i = 0; i < $scope.getSecAnalyzeTypeLimit(); i++) {
            $scope.selectedSecurity.nodes.push({
                view: '?'
            });
        }

    }
    $scope.adjustSecParams();

    // History to security queries to export
    $scope.secQueriesHistory = [];

    /*
     * Call back from the UI. when the user clicks on 'Analyze'.
     * If the placeholders are filled, populate the history and invoke the server job
     */
    $scope.analyzeSecurity = function () {
        var isValid = true;
        for (var i = 0; i < $scope.selectedSecurity.nodes.length; i++) {
            var n = $scope.selectedSecurity.nodes[i];
            if (angular.isUndefined(n.id)) {
                isValid = false;
                break;
            }
        }
        if (!isValid)
            return;

        $scope.secQueriesHistory.push($scope.selectedSecurity);
        $scope.triggerAnalyzeSecurityQuery($scope.selectedSecurity);

    }

    $scope.notifications = [];

    /*
     * Select the node in the tree. Scroll if needed.
     * Used in two places: 
     * - selecting a node in the SVG graph selects it in the tree
     * - selecting a message in the Messages window selects the corresponding node in the tree
     */
    $scope.highlightTreeNode = function (id) {
        $scope.t.jstree('deselect_all');
        $scope.t.jstree('select_node', '#' + id);

        selector = 'li[id="' + id + '"]';

        var target = angular.element(selector);
        if (target.length)
            target.get(0).scrollIntoView(true);

    }

    /*
    * Populate messages based on JSON object of security analysis
    */
    $scope.displaySecMessages = function (value, type) {
        if (angular.isDefined(value.edges)) {
            // The JSON object contains edges of interest
            value.edges.forEach(function (edge) {
                var desc = edge.srcOid + " -> " + edge.dstOid;
                $scope.displayMessages($sce.trustAsHtml(desc), type, edge.srcOid);
            });
        }
        // The JSON object contains objects of interest
        else if (angular.isDefined(value.objects)) {
            value.objects.forEach(function (obj) {
                var desc = obj.oid
                $scope.displayMessages($sce.trustAsHtml(desc), type, obj.oid);
            });
        }
    }

    /*
    * Display a Message
    * NOTE: the source has to be an object id, in order for the message to be clickable (and select the node in the tree)
    */
    $scope.displayMessages = function (desc, type, source) {
        $scope.notifications.push({
            desc: desc,
            type: type,
            source: source
        });
    }

    /*
     * JOB: ANALYZE_SEC.
     * params is the selected security analysis with the selected nodes
     */
    $scope.triggerAnalyzeSecurityQuery = function (params) {
        $scope.loading = true;
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "ANALYZE_SEC",
            params: params
        }).then(function (data) {
            $rootScope.tableTimer = $interval(function () {
                API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                    var job = data.job;
                    if (angular.isDefined(job) && job.status === 3) {
                        if (angular.isDefined(job.result.success) && job.result.success == false) {
                            // Do nothing
                        } else {
                            // Convert JSON object to messages
                            $scope.displaySecMessages(job.result, params.type);
                        }
                        $scope.cancelCheck($rootScope.tableTimer);
                    }
                })
            }, 1000)
        });
    }


    $scope.selectNode = function(id){
        var target = _.find($scope.objects, function(o) {
            return o.id === id;
        });
        //did we select a property?
        if (angular.isUndefined(target)) {
            target = _.find($scope.secProperties, function(o) {
                return o.id === id;
            });
        }
        // selectMode state: to select nodes for the security analysis
        if ($scope.selectMode && angular.isDefined(target)) {
            for (var k in $scope.selectedSecurity.nodes) {
                var n = $scope.selectedSecurity.nodes[k];
                // XXX. If we want to select things other than objects, this may no longer work
                // selecting a domain will work
                // but what if we need to select an edge
                if (angular.isUndefined(n.id)) { // an item has been selected if the id is set
                    $scope.selectedSecurity.nodes[k] = {
                        id: id,
                        view: target.text
                    }
                    $scope.$digest();
                    break
                }
            }

        }
        // Default state: selecting a node in the graph will select it in the tree
        if (!$scope.selectMode) {
            // var id = $scope.findIdOfClickedNode('graphContainer2', e.currentTarget);
            $scope.highlightTreeNode(id);
        }
    }

    /*
     * JOB: SHOW_GRAPH2.
     * Display the OGraph.
     */
    $scope.triggerShowGraph2 = function (params) {
        $scope.loading = true;
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "SHOW_GRAPH2",
            params: params
        }).then(function (data) {
            $rootScope.graphTimer2 = $interval(function () {
                API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                    var job = data.job;
                    if (angular.isDefined(job) && job.status === 3) {
                        if (angular.isDefined(job.result.success) && job.result.success == false) {
                            // Do nothing
                        } else {
                            // HACK: comment out the header being added to the SVG by DOT
                            angular.element('#graphContainer2').html('<!--' + atob(job.result));

                            // HACK: remove xlink redirection in frontend using jquery
                            angular.element('g > a').click(function (e) {
                                e.preventDefault();
                            });

                            angular.element("text").focus(function () {});
                            angular.element('svg').each(function (index, element) {
                                contextmenu.attach(element, $scope.graphMenu);
                            });
                            angular.element('#graphContainer2 > svg > g > .node, #graphContainer2 > svg > g > .cluster')
                                .click(function (e) {
                                    var id = $scope.findIdOfClickedNode('graphContainer2', e.currentTarget);
                                    $scope.selectNode(id);
                                })
                        }
                        $scope.cancelCheck($rootScope.graphTimer2);
                    }
                })
            }, 1000)
        });
    }

    /*
     * JOB: SHOW_REFINEMENT.
     * Re-populate the list of refinements from the server.
     * The server sets: RefId, refinement type, etc.
     * Important: it is used to show the heuristics that already ran before we loaded the graph.
     */
    $scope.triggerShowRefinementTable = function (params) {
        $scope.loading = true;
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "SHOW_REFINEMENT",
            params: params
        }).then(function (data) {
            $rootScope.tableTimer = $interval(function () {
                API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                    var job = data.job;
                    if (angular.isDefined(job) && job.status === 3) {
                        if (angular.isDefined(job.result.success) && job.result.success == false) {
                            // Do nothing
                        } else {
                            $scope.refinements = [];
                            // Show the refinements and heuristics
                            job.result.refinements.forEach(function (value, index) {
                                $scope.refinements.push(value);
                            })
                            job.result.heuristics.forEach(function (value, index) {
                                $scope.refinements.push(value);
                            })
                            
                            // Using the custom modified lib\contextmenu component
                            contextmenu.attach(angular.element('#refineTable').get(0), $scope.createRefineMenu());

                        }
                        $scope.cancelCheck($rootScope.tableTimer);
                    }
                })
            }, 1000)
        });
    }

    /*
     * This function creates the refinement object to send to the server.
     * Called after the d-n-d.
     */
    $scope.createRefinementParam = function (src, dst) {
        var parentId = $scope.t.jstree().get_node(dst.id).parent;
        var parent = $scope.t.jstree().get_node(parentId);

        // SplitUp
        if (src.type === 'expression') {
            var expressionId =
                src.parentObjectId + '|' +
                src.name + '|' +
                src.exprKind + '|' +
                src.exprType + '|' +
                src.enclMeth + '|' +
                src.enclType;

            return {
                type: 'SplitUp',
                refId: '?',
                srcObject: expressionId,
                srcObjectCaption: src.text,
                dstObject: parent.id,
                dstObjectCaption: parent.objectType,
                dstDomain: dst.text,
                state: 'Pending'
            }
        }

        var dstDomain = dst.text;

        var refType;
        if (dstDomain === 'owned') {
            refType = 'PushIntoOwned';
        } else if (dstDomain === 'PD') {
            refType = 'PushIntoPD';
        } else if (dstDomain === 'PARAM') {
            refType = 'PushIntoParam';
        }

        return {
            type: refType ? refType : '?',
            refId: '?',
            srcObject: src.id,
            srcObjectCaption: src.text,
            dstObject: parent.id,
            dstObjectCaption: parent.objectType,
            dstDomain: dstDomain,
            state: 'Pending'
        };
    }

    /*
     * Called by UI when the user does a d-n-d and click on the "cancel" red button.
    */
    $scope.cancelRefine = function () {
        // remove the last element
        $scope.refinements.splice($scope.refinements.length - 1);
        $scope.pendingAddRefine = false;
        $scope.t.reloadTree();
    }

    /*
     * Called by UI when the user does a d-n-d and click on the "check" green button.
     */
    $scope.addRefine = function () {
        // The last refinement object is the newly added one
        var param = $scope.refinements[$scope.refinements.length - 1];
        $scope.pendingAddRefine = false;
        $scope.triggerAddRefinement(param);
    }
    
     /*
     * Called by UI when the user clicks on the "apply" green button.
     */
    $scope.submitRefine = function () {
        if (!$scope.pendingAddRefine && $scope.isPendingInList()) {
            $scope.triggerDoRefinement();
        }
    }


    // Drag-n-drop release
    angular.element(document).on('dnd_stop.vakata', function (e, data, s) {
        if (angular.isDefined($scope.refineMove.src)) {

            var refineParam = $scope.createRefinementParam($scope.refineMove.src, $scope.refineMove.dst);
            //            console.log($scope.refinements);
            $scope.refinements.push(refineParam);
            $scope.pendingAddRefine = true;
            // The object we created refineParam does not have the $$hashKey
            // It is required for arrays, not variables.
            // Add required internal hashkeys $$hashKey: "object:nn" so the UI can display these objects
            $scope.$digest();
        }
    });

    $scope.refinementProcessing = false;

    /*
    * JOB: REFINE_GRAPH.
    * Handle the "apply" button.
    */
    $scope.triggerDoRefinement = function () {
        $scope.refinementProcessing = true;
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "REFINE_GRAPH",
        }).then(function (data) {
            $rootScope.refineProcessingTimer = $interval(function () {
                API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                    var job = data.job;
                    if (angular.isDefined(job) && job.status === 3) {
                        // If failure, then just update the refinement table
                        // By convention, the server must return false if refinement is unsupported
                        // To show e.g., unsupported refinement
                        // The tree is restored to the previous state before the refinement.
                        if (angular.isDefined(job.result.success) && job.result.success == false) {
                            $scope.triggerShowRefinementTable();
                            $scope.triggerPopulateTree();
                        } else {
                            // If success, reload the whole page, becasue everything changed
                            // (the OGraph changed, the tree changed, the SVG changed, etc.)
                            $route.reload();
                        }
                        $scope.refinementProcessing = false;
                        $scope.cancelCheck($rootScope.refineProcessingTimer);
                    }
                })
            }, 3000) // Check every 3 seconds.
        });
    }

    /*
     * JOB: LOAD_STATE
     * Retrieve the OGraphState from the server and store it.
     * Then use it to set properties on objects.
     */
    $scope.triggerLoadStates = function () {
        $scope.loading = true;
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "LOAD_STATE",
        }).then(function (data) {
            $rootScope.loadStateTimer = $interval(function () {
                API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                    var job = data.job;
                    if (angular.isDefined(job) && job.status === 3) {
                        if (angular.isDefined(job.result.success) && job.result.success == false) {} else {
                            $scope.OStates = job.result;
                        }
                        $scope.cancelCheck($rootScope.loadStateTimer);
                    }
                })
            }, 1000)
        });
    }

    /*
     * JOB: SAVE_STATE
     * Send modified the OGraphState back to the server.
     * Expects the server to re-generate the graphs (both SVG and SVG2) with the updated information.
     * Then reloads the SVG and SVG2
     * XXX. Maybe do things more lazily here, because either SVG or SVG2 may not be visible.
     * Ideally, update only the visible graph based on the currently active pane.
     */
    $scope.triggerSaveStates = function (params) {
        $scope.loading = true;
        API.addJob({
            instanceId: $routeParams.instanceId,
            jobName: "SAVE_STATE",
            params: params
        }).then(function (data) {
            $rootScope.loadStateTimer = $interval(function () {
                API.checkJob($routeParams.instanceId, data.job.id).then(function (data) {
                    var job = data.job;
                    if (angular.isDefined(job) && job.status === 3) {
                        if (angular.isDefined(job.result.success) && job.result.success == false) {} else {
                            $scope.triggerShowGraph({});
                            $scope.triggerShowGraph2({});
                            $scope.t.reloadTree();
                        }
                        angular.element('#objectPropDialog').modal('hide');
                        $scope.cancelCheck($rootScope.loadStateTimer);
                    }
                })
            }, 1000)
        });
    }

    /*
    * Show the busy graphic while the refinement is still being processed
    */
    $scope.$watch('refinementProcessing', function (newValue) {
        if (newValue) {
            angular.element('#refineTable').addClass('loading-container');
        } else {
            angular.element('#refineTable').removeClass('loading-container');
        }
    });


    /*
     * Zoom in the SVG by increasing SVG height & width by 110%
     */
    $scope.zoomIn = function () {
        var selector = '#' + $scope.currentGraphId + ' > svg';
        angular.element(selector)
            .attr('height', (
                parseInt(angular.element(selector).attr('height')) * 1.1).toString() + 'pt');

        angular.element(selector)
            .attr('width', (
                parseInt(angular.element(selector).attr('width')) * 1.1).toString() + 'pt');
    }

    /*
     * Zoom out the SVG by decreasing SVG height & width by 90%
     */
    $scope.zoomOut = function () {
        var selector = '#' + $scope.currentGraphId + ' > svg';
        angular.element(selector)
            .attr('height', (
                parseInt(angular.element(selector).attr('height')) * 0.9).toString() + 'pt');

        angular.element(selector)
            .attr('width', (
                parseInt(angular.element(selector).attr('width')) * 0.9).toString() + 'pt');
    }

    /*
    * Initialze the page at loading.
    * XXX. Can do things more lazily:
    * - show one graph (for the default pane)
    * - could even not show the code initially
    */
    $scope.init = function () {
        $scope.triggerLoadStates();
        $scope.triggerShowRefinementTable();
        $scope.triggerPopulateTree();
        $scope.triggerShowCode({});
        $scope.triggerShowGraph({});
        $scope.triggerShowGraph2({});
    }

    $scope.init();

})