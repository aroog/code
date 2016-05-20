webApp.factory('API', function ($http, $q, endpoint) {
  return {
    listPlugin : function(){
        var deferred = $q.defer();
        $http.get(endpoint + 'web/list').success(function(data){
            deferred.resolve(data);
        })
        return deferred.promise;        
    },
    addJob: function(data){
        var deferred = $q.defer();
        $http.post(endpoint + 'web/job', data).success(function(data){
            deferred.resolve(data);
        })
        return deferred.promise;
    },
    checkJob: function(instanceId, jobId){
        var deferred = $q.defer();
        $http.get(endpoint + 'web/job?instanceId=' + instanceId + '&jobId=' + jobId).
        success(function(data){
            deferred.resolve(data);
        })
        return deferred.promise;
    },
//    Deprecated: No longer store job results as files so we don't need to issue another request to get the file
//     
//    getJSON: function(path){
//        var deferred = $q.defer();
//        $http.get(endpoint + path).
//        success(function(data){
//            deferred.resolve(data);
//        })
//        return deferred.promise;
//    }
  }
});