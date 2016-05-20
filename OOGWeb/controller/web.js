var randomstring = require("randomstring"),
    util = require('util'),
    persist = require('../util/persist.js').persist;

exports.routes = (function(){    
    return {
        // Return list of subscribers. Each instance has a list of its jobs.
        list: function(req, res) {
            res.send(req.app.eclipseSubs);
        },
        // Check if instanceId is valid
        addJob: function(req, res){
            if(util.isUndefined(req.body.instanceId)){
                res.status(400);
                return res.send({error: 'instanceId is undefined'});
            }
            // req.body is the JSON object, produced by the body parser middleware
            var instanceId = req.body.instanceId;
            
            if(util.isUndefined(req.body.jobName)){
                res.status(400);
                return res.send({error: 'jobName is undefined'});
            }
            var jobName = req.body.jobName;
            
            // Check if instanceId is in our database                                
            if(util.isUndefined(req.app.eclipseSubs[instanceId])){
                res.status(400);
                return res.send({error: 'instanceId does not exist'});
            }else{
                // Add a new job
                var job = {
                    id: randomstring.generate(30),
                    name: jobName,
                    params: req.body.params,
                    status: 0, // See JOB_STATUS.txt
                    created: new Date()
                }                
                req.app.eclipseSubs[instanceId].jobs.push(job);
                persist.sync(req);
                res.send({msg: "Job added for processing successfully", job: job});
            }
        },
        // Call back from timer: check if instanceId is valid, jobId is valid
        // Update the parts of the UI related to the job
        checkJob: function(req, res){
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
                var job;
                for(var i in jobs){
                    if(jobs[i].id === jobId){
                        job = jobs[i];
                        if(job.status === 2){ // Job processed by plugin
                            req.app.eclipseSubs[instanceId].jobs[i].status++;
                            persist.sync(req);
                        }
                    }
                }
                if(util.isUndefined(job)){
                    res.send({error: "Job not found"})
                }
                else{
                    // Send job to the web client to refresh the UI associated with a job
                    res.send({job: job});    
                }                
            }            
        }
    };
})();