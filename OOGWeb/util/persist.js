// Module to persist the in-memory db back to CouchDB
var persist = (function(){
    var revisionCounter = 0;
    var sync = function(req){
        var app = req.app;  
        var nano = app.nano;  
        var eclipse = nano.use('eclipse');
        // Use nano to do get/post on HTTP-based DB
        eclipse.get("eclipseSubs", function(error, existing){
            if(!error) 
                app.eclipseSubs._rev = existing._rev;
            
            // Keep track of the last 10 revisions. After that, compact the db.
            eclipse.insert(app.eclipseSubs, "eclipseSubs", function(error, eclipseSubs){                
                if(!error && revisionCounter++ === 10){
                    nano.db.compact('eclipse');   
                    revisionCounter = 0
                }
            });    
        });
    }
    
    return {
        sync : sync
    };
})();


// Change this middleware to a reusable module and only use it when needed
// because of performance issue:
// -- CouchDB works over http.
// -- We have lots of pulling requests without any data change.
// -- On each pulling request (lots of them in a second) we consume lots of http request to couchDb
//function persist(req, res, next){
//    var app = req.app;
//    var nano = app.nano;  
//    var eclipse = nano.use('eclipse');
//    res.on('finish', function(){      
//        eclipse.get("eclipseSubs", function(error, existing){
//            if(!error) 
//                app.eclipseSubs._rev = existing._rev;
//            
//            eclipse.insert(app.eclipseSubs, "eclipseSubs", function(error, eclipseSubs){
//                if(!error)
//                    nano.db.compact('eclipse');
//            });    
//        });
//    });
//    next();
//}

exports.persist = persist;