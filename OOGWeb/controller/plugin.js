var util = require('util'),
    fs = require('fs'),
    persist = require('../util/persist.js').persist;

exports.routes = (function(){    
    return {
        // Eclipse plugin calls this API when it runs to subscribe itself
        subscribe: function(req, res, next){      
            if(util.isUndefined(req.body.projectName)){   
                res.status(400);
                return res.send({error: 'projectName is undefined'});
            }
            var prjName = req.body.projectName;
            
            if(util.isUndefined(req.body.instanceId)){
                res.status(400);
                return res.send({error: 'instanceId is undefined'});
            }
            var instanceId = req.body.instanceId;
            
            if(util.isUndefined(req.app.eclipseSubs[instanceId])){
                req.app.eclipseSubs[instanceId] = { project: prjName , jobs: []};
                persist.sync(req);
                res.send({msg: "Plugin subscribed successfully"});
            }else{
                res.status(401);
                return res.send({error: 'Plugin with this instancecId is already subscribed.'});
            }            
        },
        // Eclipse plugin calls this to obtain the list of jobs available for processing
        fetchJobs: function(req, res){
            if(util.isUndefined(req.query.instanceId)){
                res.status(400);
                return res.send({error: 'instanceId is undefined'});
            }
            var instanceId = req.query.instanceId;
            
            if(util.isUndefined(req.app.eclipseSubs[instanceId])){
                res.status(400);
                return res.send({error: 'instanceId does not exist'});
            }else{
                var jobs = req.app.eclipseSubs[instanceId].jobs;
                for(var i in jobs){
                    if(jobs[i].status === 0){
                        req.app.eclipseSubs[instanceId].jobs[i].status++;
                        persist.sync(req);
                    }
                }
                return res.send({ jobs: req.app.eclipseSubs[instanceId].jobs});
            }            
        },
        // Eclipse plugin calls this to set the JSON result and set the status
        updateJob: function(req, res){
            if(util.isUndefined(req.query.instanceId)){
                res.status(400);
                return res.send({error: 'instanceId is undefined'});
            }
            var instanceId = req.query.instanceId;
            
            if(util.isUndefined(req.query.jobId)){
                res.status(400);
                return res.send({error: 'jobId is undefined'});
            }
            var jobId = req.query.jobId;
            
            if(util.isUndefined(req.app.eclipseSubs[instanceId])){
                res.status(400);
                return res.send({error: 'instanceId does not exist'});
            }else{                                
                var jobs = req.app.eclipseSubs[instanceId].jobs;
                for(var i in jobs){                    
                    if(jobs[i].id === jobId){
                        req.app.eclipseSubs[instanceId].jobs[i].status++;
                        req.app.eclipseSubs[instanceId].jobs[i].updated = new Date();
                        // req.file is the data stored as a file temporarily on the server
                        if(util.isObject(req.file)){
//                            req.app.eclipseSubs[instanceId].jobs[i].result = "uploads/" + req.file.filename;
                            var filePath = "tmp/" + req.file.filename;
                            var result_data = fs.readFileSync(filePath).toString();
                            try{
                                result_data = JSON.parse(result_data)
                            }
                            catch(e) {
                                // Cannot convert to JSON. So keep as a string
                            }
                            // Remove the temp file
                            fs.unlinkSync(filePath);                            
                            req.app.eclipseSubs[instanceId].jobs[i].result = result_data;
                        }                        
                        persist.sync(req);
                    }
                }
                return res.send({jobs: req.app.eclipseSubs[instanceId].jobs});
            } 
        }
    };
})();