"use strict";

const SOCKET_IO = require('socket.io');
// const HTTP = require('http');
const HTTPS = require('https');
const fs = require('fs');

//models
const UserClass = require('./models/user');
const User = new UserClass();
const StudentClass = require('./models/student');
const Student = new StudentClass();
const _ClassClass = require('./models/class');
const _Class = new _ClassClass();
const AttachmentClass = require('./models/attachment');
const Attachment = new AttachmentClass();
const BillingClass = require('./models/billing');
const Billing = new BillingClass();

// const server = HTTP.createServer((req, res)=>{
// }).listen(8081);
// const io = SOCKET_IO.listen(server);

//https server
const options = {
  ca: fs.readFileSync('./SSL/ca_bundle.crt'),
  key: fs.readFileSync('./SSL/private.key'),
  cert: fs.readFileSync('./SSL/certificate.crt'),
};
const httpsServer = HTTPS.createServer(options);
httpsServer.listen(8081, ()=>{
  console.log('API Server Start');
});
const io = SOCKET_IO.listen(httpsServer);

//mongoose
const mongoose = require('mongoose');
mongoose.connect('mongodb://localhost/cms')

io.sockets.on('connection', function(socket){
  console.log("a client connected.");

  socket.on('disconnect', ()=>{
    console.log('a client disconnect.');
  });

  //user functions
  socket.on('getUserByName', (data)=>{
    console.log('client get user:' + JSON.stringify(data));
    const {token} = data;
    User.getUserByName(socket, token, processObjectAttributes(data));
  });


  //class Schedule functions
  socket.on('uploadClassSchedule', (data)=>{
    console.log('client upload class schedule:' + JSON.stringify(data));
    const {token} = data;
    _Class.uploadClassSchedule(socket, token, processObjectAttributes(data));
  });


  socket.on('deleteClassSchedule', (data)=>{
    console.log('client delete class schedule:' + JSON.stringify(data));
    const {token} = data;
    _Class.deleteClassSchedule(socket, token, processObjectAttributes(data));
  });

  socket.on('getMonthClassSchedule', (data)=>{
    console.log('client get month class schedule:' + JSON.stringify(data));
    const {token} = data;
    _Class.getMonthClassSchedule(socket, token, processObjectAttributes(data));
  });

  socket.on('getDayClassSchedule', (data)=>{
    console.log('client get day class schedule:' + JSON.stringify(data));
    const {token} = data;
    _Class.getDayClassSchedule(socket, token, processObjectAttributes(data));
  });

  // socket.on('addClassSchedule', (data)=>{
  //   console.log('client add class schedule:' + JSON.stringify(data));
  //   const {token} = data;
  //   _Class.addClassSchedule(socket, token, processObjectAttributes(data));
  // });

  //billing functions
  socket.on('getSMSLog', (data)=>{
    console.log('client get sms log:' + JSON.stringify(data));
    const {token} = data;
    Billing.getSMSLog(socket, token, processObjectAttributes(data));
  });

  socket.on('uploadSMSLog', (data)=>{
    console.log('client upload sms log:' + JSON.stringify(data));
    const {token} = data;
    Billing.uploadSMSLog(socket, token, processObjectAttributes(data));
  });

  socket.on('updateBilling', (data)=>{
    console.log('client update billing:' + JSON.stringify(data));
    const {token} = data;
    Billing.updateBilling(socket, token, processObjectAttributes(data));
  });

  socket.on('getBilling', (data)=>{
    console.log('client get billing:' + JSON.stringify(data));
    const {token} = data;
    Billing.getBilling(socket, token, processObjectAttributes(data));
  });

  socket.on('getBillings', (data)=>{
    console.log('client get billings:' + JSON.stringify(data));
    const {token} = data;
    Billing.getBillings(socket, token, processObjectAttributes(data));
  });

//upload image functions
  socket.on('uploadProfileImage', (data)=>{
    console.log('client upload profile image:' + JSON.stringify(data));
    const {token} = data;
    User.uploadProfileImage(socket, token, processObjectAttributes(data));
  });

  socket.on('uploadStudentProfileImage', (data)=>{
    console.log('client upload student profile image:' + JSON.stringify(data));
    const {token} = data;
    Student.uploadStudentProfileImage(socket, token, processObjectAttributes(data));
  });

  socket.on('uploadFile', (data)=>{
    console.log('client upload file:' + JSON.stringify(data));
    const {token} = data;
    Attachment.uploadFile(socket, token, processObjectAttributes(data));
  });

  socket.on('deleteAttachment', (data)=>{
    const {token, _id} = data;
  console.log('client delete attachment token:' + JSON.stringify(data));

    Attachment.deleteAttachment(socket, token, _id);
  });

  //class funcitons start

  socket.on('getStudentClass', (data)=>{
    const {token, student_id} = data;
    console.log('client get student class token:' + JSON.stringify(data));
    Student.getStudentClass(socket, token, student_id);
  });

  socket.on('searchStudentAttachments', (data)=>{
    const {token} = data;
    console.log('client search student attachments token:' + JSON.stringify(data));
    Attachment.searchStudentAttachments(socket, token, processObjectAttributes(data));
  });

  socket.on('searchAttachmentsByClassId', (data)=>{
    const {token, class_id} = data;
    console.log('client get attachments token:' + JSON.stringify(data));
    Attachment.searchAttachmentsByClassId(socket, token, class_id);
  });

    socket.on('searchAttachments', (data)=>{
      const {token, query} = data;
      console.log('client get attachments token:' + JSON.stringify(data));
      Attachment.searchAttachments(socket, token, query);
    });

    socket.on('recommendClass', (data)=>{
      const {token, student_id} = data;
      console.log('client get recommend class :' + JSON.stringify(data));
      _Class.recommendClass(socket, token, student_id);
    });

    socket.on('editClass', (data)=>{
      console.log('client edit class:' + JSON.stringify(data));
      const {token, class_id} = data;
      _Class.editClass(socket, token, class_id, processObjectAttributes(data));
      // console.log(data);
    });

    socket.on('getClass', (data)=>{
      console.log('client get class:' + JSON.stringify(data));
      const {token, class_id} = data;
      _Class.getClass(socket, token, class_id);
      // console.log(data);
    });

  socket.on('addClass', (data)=>{
    console.log('client add class:' + JSON.stringify(data));
    const {token} = data;
    _Class.addClass(socket, token, processObjectAttributes(data));
    // console.log(data);
  });

  socket.on('searchClasses', (data)=>{
    console.log('client search class:' + JSON.stringify(data));
    const {token} = data;
    _Class.searchClasses(socket, token, processObjectAttributes(data));
  });

  socket.on('joinClass', (data)=>{
    console.log('client verify join class:' + JSON.stringify(data));
    const {token} = data;
    _Class.joinClass(socket, token, processObjectAttributes(data), null);
  });

  socket.on('joinClass2', (data)=>{
    console.log('client verify join class 2:' + JSON.stringify(data));
    const {token} = data;
    _Class.joinClass(socket, token, processObjectAttributes(data), "joinClass2");
  });

  //student functions
  socket.on('getStudents', (data)=>{
    console.log('client getStudents:' + JSON.stringify(data));
    const {token} = data;
    Student.getStudents(socket, token, processObjectAttributes(data));
  });

  socket.on('addStudent', (data)=>{
    console.log('client add student:' + JSON.stringify(data));
    const {token} = data;
    Student.addStudent(socket, token, processObjectAttributes(data));
    // console.log(data);
  });

  socket.on('getStudentByStudentId', (data)=>{
    console.log('client getStudentByStudentId:' + JSON.stringify(data));
    const {token, student_id} = data;
    Student.getStudentByStudentId(socket, token, student_id);
  })

  socket.on('editStudent', (data)=>{
    console.log('client editStudent:' + JSON.stringify(data));
    const {token, student_id} = data;
    Student.editStudent(socket, token, student_id, data);
    // console.log(data);
  });

  socket.on('deleteStudent', (data)=>{
    console.log('client delete Student:' + JSON.stringify(data));
    const {token, student_id} = data;
    Student.deleteStudent(socket, token, student_id);
  })

  //profile functions
  socket.on('getProfile', (data)=>{
    const {token} = data;
    console.log('client get_profile:' + token);
    User.getProfile(socket, token);
  });

  socket.on('updateProfile', (data)=>{
    console.log('client update_profile:' + JSON.stringify(data));
    User.updateProfile(socket, data);
  });

  //login functions
  socket.on('registerAccount', (data)=>{
    const {email, password} = data;
    console.log('client register account:' + email + "," + password);
    User.registerAccount(socket, email, password);
  });


  socket.on('login', (data)=>{
    const {email, password} = data;
    console.log('client login:' + email + "," + password);
    User.verifyLogin(socket, email, password);
  });

  socket.on('loginToken', (data)=>{
    const {token} = data;
    console.log('client login token:' + token);
    _.verifyLoginToken(socket, token);
  });

//delete field
  function processObjectAttributes(data){
    delete data.token;
    return data;
  }

});
