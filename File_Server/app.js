var express = require('express');
var https = require('https');
const fs = require('fs');
var app = express();

// app.get('/public', function (req, res) {
//   res.send('Hello World!');
// });

const options = {
  ca: fs.readFileSync('./SSL/ca_bundle.crt'),
  key: fs.readFileSync('./SSL/private.key'),
  cert: fs.readFileSync('./SSL/certificate.crt'),
};

const httpsServer = https.createServer(options, app);
httpsServer.listen(8082, ()=>{
  console.log('File Server Start');
});


app.use('/public', express.static('../API_Server/attachments/'));
