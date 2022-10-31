const error_message = require('../variables/error_message');
const error_code = require('../variables/error_code');
const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const fs = require('fs');
const path = require('path');

const FILE_PATH_URL = "";

const FILE_TYPE = {
  "PNG": "image/png",
  "MP4": "movie/mp4",
}

const FILE_EXTENSION = {
  "PNG": ".png",
  "MP4": ".mp4",
}


const AttachmentSchema = new Schema({
  //billing_id: {type: String, required: true, index: {unique: true} },
  student: {type: Schema.Types.ObjectId, ref: 'students'},
  class: {type: Schema.Types.ObjectId, ref: 'classes'},
  teacher: {type: Schema.Types.ObjectId, ref: 'users'},
  description: String,
  file_name: String,
  file_extension: String, // refer to FILE_EXTENSION
  file_type: String,  // refer to FILE_TYPE
  deleted: {type: Boolean, default: false},
  created_at: {type: Date, default: Date.now},
  modified_at: Date,
});

const Attachment = mongoose.model('attachments', AttachmentSchema);

module.exports = class AttachmentClass {
  constructor(){

  }

  deleteAttachment(socket, token, _id){
    Attachment.find({_id: _id, deleted: {$in: [null,false]}}).exec((err,attachments)=>{
      if (err || attachments.length != 1){
        socket.emit('deleteAttachment', {result: false, error_message: error_message.pls_try_again_later});
      } else {
        const a = attachments[0];
        a.deleted = true;
        a.save((err)=>{
          if (err){
            socket.emit('deleteAttachment', {result: false, error_message: error_message.pls_try_again_later});
          } else {
            socket.emit('deleteAttachment', {result: true});
          }
        });
      }
    });
  }


  uploadFile(socket, token, data){

    const _ClassClass = require('./class');
    const _Class = new _ClassClass();
    const _ClassModel = _Class.getModel();
    const StudentClass = require('./student');
    const Student = new StudentClass();
    const StudentModel = Student.getModel();

    const {student_id, class_id, video, image, description} = data;

      let student_condition;
      if (student_id != undefined && student_id != null){
        student_condition = {path: 'students', match: { student_id: student_id } };
      } else {
        student_condition = {path: 'teachers'};
      }

      if (class_id == undefined || class_id == ""){

                    StudentModel.find({student_id: student_id, deleted: {$in: [null, false]}}).exec((err,students)=>{

                      if (err){
                        socket.emit('uploadFile', {result: false, error_message: error_message.pls_try_again_later});
                      } else {
                        const s = students[0];

                      const file_name = new Date().getTime().toString();
                      let file_content;
                      let file_path;
                      if (video != undefined && video != ""){
                        file_content = video
                        file_path = path.join(__dirname, "..", "attachments", file_name) + FILE_EXTENSION.MP4;
                      } else if (image != undefined && image != ""){
                        file_content = image;
                        file_path = path.join(__dirname, "..", "attachments", file_name) + FILE_EXTENSION.PNG;
                      }

                      fs.writeFile(file_path, file_content, (err)=>{
                        if (err){
                          socket.emit('uploadFile', {result: false, error_message: error_message.upload_file_failed});
                        } else {
                          const attach = new Attachment();
                          attach.student = s;
                          attach.file_name = file_name;


                          if (video != undefined && video != null){
                            attach.file_extension = FILE_EXTENSION.MP4;
                          } else if (image != undefined && image != null){
                            attach.file_extension = FILE_EXTENSION.PNG;
                          }
                          attach.description = description;
                          attach.save((err, a)=>{
                            if (err){
                              console.log(err);
                              socket.emit('uploadFile', {result: false, error_message: error_message.pls_try_again_later});
                            } else {

                                socket.emit('uploadFile', {result: true});
                              // console.log('uploadImage');
                            }
                          });
                        }
                      });
                    }
                });

      } else {

        _ClassModel.find({class_id: class_id, deleted: {$in: [null, false]}}).populate('attachments').populate(student_condition).exec((err, classes)=>{
          if (err){
            console.log(err);
            socket.emit('uploadFile', {result: false, error_message: error_message.pls_try_again_later});
          } else if (classes.length != 1) {
            socket.emit('uploadFile', {result: false, error_message: error_message.class_not_exist});
          } else {

            const file_name = new Date().getTime().toString();
            let file_content;
            let file_path;
            if (video != undefined && video != ""){
              file_content = video
              file_path = path.join(__dirname, "..", "attachments", file_name) + FILE_EXTENSION.MP4;
            } else if (image != undefined && image != ""){
              file_content = image;
              file_path = path.join(__dirname, "..", "attachments", file_name) + FILE_EXTENSION.PNG;
            }

            const c = classes[0];

            //console.log(path);
            fs.writeFile(file_path, file_content, (err)=>{
              if (err){
                socket.emit('uploadFile', {result: false, error_message: error_message.upload_file_failed});
              } else {
                const attach = new Attachment();
                attach.class = c;
                attach.file_name = file_name;

                if (student_id != undefined && student_id != null){
                  //attach.student = student_id;

                  StudentModel.find({student_id: student_id}).exec((err, students)=>{

                    if (err){
                      socket.emit('uploadFile', {result: false, error_message: error_message.student_not_exist});
                    } else {
                      const s = students[0];
                      attach.student = s;

                      if (video != undefined && video != null){
                        attach.file_extension = FILE_EXTENSION.MP4;
                      } else if (image != undefined && image != null){
                        attach.file_extension = FILE_EXTENSION.PNG;
                      }
                      attach.description = description;
                      attach.save((err, a)=>{
                        if (err){
                          console.log(err);
                          socket.emit('uploadFile', {result: false, error_message: error_message.pls_try_again_later});
                        } else {
                          c.attachments.push(a._id);


                          c.save((err)=>{
                            if (err) {
                              console.log("test3");
                              socket.emit('uploadFile', {result: false, error_message: error_message.pls_try_again_later});
                            } else {

                                socket.emit('uploadFile', {result: true});

                              //socket.emit('searchAttachments', {result: true, attachments: new_class.attachments});
                            }
                          });

                          // console.log('uploadImage');
                        }
                      });
                    }




                  });



                } else {


                  if (video != undefined && video != null){
                    attach.file_extension = FILE_EXTENSION.MP4;
                  } else if (image != undefined && image != null){
                    attach.file_extension = FILE_EXTENSION.PNG;
                  }
                  attach.description = description;
                  attach.save((err, a)=>{
                    if (err){
                      console.log(err);
                      socket.emit('uploadFile', {result: false, error_message: error_message.pls_try_again_later});
                    } else {
                      c.attachments.push(a._id);


                      c.save((err)=>{
                        if (err) {
                          console.log("test3");
                          socket.emit('uploadFile', {result: false, error_message: error_message.pls_try_again_later});
                        } else {

                            socket.emit('uploadFile', {result: true});

                          //socket.emit('searchAttachments', {result: true, attachments: new_class.attachments});
                        }
                      });

                      // console.log('uploadImage');
                    }
                  });

                }


              }
            });
          }
        });

      }


  }

  searchAttachmentsByClassId(socket, token, class_id){

    const _ClassClass = require('./class');
    const _Class = new _ClassClass();
    const _ClassModel = _Class.getModel();

    _ClassModel.find({class_id: class_id})
    .populate({path:'attachments', match:{deleted: {$in: [null,false]}},populate: [{path:'class', match:{deleted: {$in: [null,false]}}},{path:'student', match:{deleted: {$in: [null,false]}}}]})
    .exec((err, classes)=>{
      if (err){
        socket.emit('searchAttachments', {result: false, error_message: error_message.pls_try_again_later});
      } else if (classes.length != 1){
        socket.emit('searchAttachments', {result: false, error_message: error_message.class_not_exist});
      } else {
        const c = classes[0];
        const attachments = c.attachments;
        if (attachments == undefined || attachments.length == 0){
          socket.emit('searchAttachments', {result: false, error_message: error_message.attachment_not_exist});
        } else {
          socket.emit('searchAttachments', {result: true, attachments: c.attachments});
        }

      }
    });
  }

  searchStudentAttachments(socket, token, data){

    const StudentClass = require('./student');
    const Student = new StudentClass();
    const StudentModel = Student.getModel();


    const {student_id, class_id} = data;
    let class_condition;
    if (class_id != undefined && class_id != null){
      class_condition = {path: 'classes', match: {class_id: class_id}};
    } else {
      class_condition = {path: 'classes'};
    }

    StudentModel.find({student_id:student_id, deleted: {$in: [null, false]}})
    .populate(class_condition)
    .exec((err, students)=>{
      if (err){
        //console.log(err);
        socket.emit('searchAttachments', {result: false, error_message: error_message.pls_try_again_later});
      } else if (students.length != 1) {
        socket.emit('searchAttachments', {result: false, error_message: error_message.student_not_exist});
      } else {
        const s = students[0];
        Attachment.find({$or: [{class: {$in: s.classes}, student: {$in: s}}, {student: {$in:s}}] })
        .populate({path:'class',match:{deleted: {$in: [null,false]}}})
        .populate({path:'student', match:{deleted: {$in: [null,false]}}}).exec((err,as)=>{

        socket.emit('searchAttachments', {result: true, attachments: as});
        });
      }
    });
  }

  searchAttachments(socket, token, query){
    const query_array = query.split(" ");

    let temp_student_condition = [];
    let temp_class_condition = [];
    query_array.forEach((q)=>{
      temp_student_condition.push({$or: [{student_id: {$regex: q, $options: "i"}}, {firstname:  {$regex: q, $options: "i"}}, {lastname: {$regex: q, $options: "i"}}]});
      temp_class_condition.push({class_id: {$regex: q, $options: "i"}});
    });


    const _ClassClass = require('./class');
    const _Class = new _ClassClass();
    const _ClassModel = _Class.getModel();

    const StudentClass = require('./student');
    const Student = new StudentClass();
    const StudentModel = Student.getModel();

    _ClassModel.find({$and: [{deleted: {$in: [null, false]}}, {$or: temp_class_condition}]}).exec((err,cs)=>{
      // if (err){
      //   socket.emit('searchAttachments', {result: false, error_message: error_message.pls_try_again_later});
      // } else {

        StudentModel.find({$and: [{deleted: {$in: [null, false]}}, {$or: temp_student_condition}]}).exec((err,ss)=>{
          // if (err){
          //   socket.emit('searchAttachments', {result: false, error_message: error_message.pls_try_again_later});
          // } else {

            Attachment.find({deleted: {$in: [null, false]}, $or: [{student: {$in: ss}}, {class: {$in: cs}}]})
            .populate({path:'class',match:{deleted: {$in: [null,false]}}})
            .populate({path:'student', match:{deleted: {$in: [null,false]}}})
            .exec((err, as)=>{

              // if (err){
              //   socket.emit('searchAttachments', {result: false, error_message: error_message.pls_try_again_later});
              // } else {

                socket.emit('searchAttachments', {result: true, attachments: as});

              // }

            });
          // }

        });

      // }


    });

    // Attachment.find({deleted: {$in: [null, false]}})
    // .populate({path:'student', match: {$and: temp_student_condition}})
    // .populate({path:'class',match: {$and: temp_class_condition}})
    // .exec((err,attachments)=>{
    //   if (err){
    //     socket.emit('searchAttachments', {result: false, error_message: error_message.pls_try_again_later});
    //   } else {
    //     if (attachments.length > 0){
    //       const as = [];
    //       attachments.forEach((a)=>{
    //         if (a.student != null || a.class != null){
    //           as.push(a);
    //         }
    //       });
    //
    //       //console.log(as);
    //       socket.emit('searchAttachments', {result: true, attachments: as});
    //     } else {
    //       socket.emit('searchAttachments', {result: false, error_message: error_message.error_empty_result});
    //     }
    //
    //   }
    // });
  }

  getModel(){
    return Attachment;
  }

}
