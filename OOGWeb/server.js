// Express JS framework in Node.js
var express = require('express'),
    // Convert body of HTTP request to JSON
    bodyParser = require('body-parser'),
    
    multer = require('multer'),
    
    // File handling (create, remove file/folder)
    fs = require('fs'),
    
    upload = multer({ dest: 'tmp/' }),
    
    // Connect to Couch DB
    // TODO: Change IP address and port to reflect where CouchDB is installed
    nano = require('nano')('http://localhost:5984'),
    
    webCtrls = require('./controller/web.js'),
    
    pluginCtrls = require('./controller/plugin.js');

var app = express();

//Middleware
app.use('/', express.static('frontend')); // serve up the UI
//app.use('/uploads', express.static('tmp')); // not used any longer (was used for uploading files)

//Configuration
app.set('etag', false); // Disable caching since we are constantly changing the graph/state

//Routing
///Web (controllers for the Web UI)
app.get('/web/list', webCtrls.routes.list);
app.post('/web/job', bodyParser.json(), webCtrls.routes.addJob);
app.get('/web/job', webCtrls.routes.checkJob);
///Plugin
app.post('/plugin/subscribe', bodyParser.json(), pluginCtrls.routes.subscribe);
app.get('/plugin/job', pluginCtrls.routes.fetchJobs);
app.post('/plugin/job', upload.single('result'), pluginCtrls.routes.updateJob);



//Initialization
// Temp directory for the body parser
fs.mkdir('tmp',function(e){});

// Eclipse subscribers
// Always start with a clean db and fresh data.
app.eclipseSubs = {};
app.nano = nano;
app.nano.db.create('eclipse');    

// Name of the database in CouchDB
var eclipse = nano.use('eclipse');

// We don't need server to reload previous data again
//eclipse.get('eclipseSubs', function(err, eclipseSubs) {
//    if (!err)
//        app.eclipseSubs = eclipseSubs;
//});    

// Select a port for the server
var port = 8080;
if( process.argv.length > 2){
    port = process.argv[2];
}

// Overwrite the file app.js (with the appropriate port number)
var t = fs.readFileSync('frontend/app.js.original').toString();
var  t = t.replace(/<PORT>/g, port);
fs.writeFileSync('frontend/app.js', t);

// Run the server on the port
var server = app.listen(port, function () {
  var host = server.address().address;
  if(host === '::'){ host = 'localhost'}
  
  var port = server.address().port;
  console.log('OOG app listening at http://%s:%s', host, port);
});