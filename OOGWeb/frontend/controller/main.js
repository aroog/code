webApp.controller('mainController', function($scope, $interval, $rootScope, API) {
    $interval.cancel($rootScope.treeTimer);   
    $interval.cancel($rootScope.tableTimer);
    $interval.cancel($rootScope.codeTimer);
    $interval.cancel($rootScope.graphTimer); 
    $interval.cancel($rootScope.graphTimer2); 
    $interval.cancel($rootScope.refineProcessingTimer); 
    $interval.cancel($rootScope.loadStateTimer); 
    
    

  
    
    $scope.populatePluginList = function(){
        API.listPlugin().then(function(data){
            $scope.plugins = [];
            for(var id in data){
                if(angular.isObject(data[id])){
                    $scope.plugins.push({
                        id: id,
                        project: data[id].project,
                        jobs: data[id].jobs
                    })
                }                
            }
//            console.log($scope.plugins);
        })
    }
    $scope.populatePluginList();
    $rootScope.pluginListTimer = $interval($scope.populatePluginList, 3000);    
})