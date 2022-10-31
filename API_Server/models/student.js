const error_message = require('../variables/error_message');
const mongoose = require('mongoose');
const Schema = mongoose.Schema;



// const _ClassClass = require('./class');
// const _Class = new _ClassClass();
// const ClassModel = _Class.getModel();

//student db schema
const StudentSchema = new Schema({
  student_id: {type: String, required: true, index: {unique: true} },
  hkid: String,
  email: String,
  lastname: String,
  firstname: String,
  mobile_no: String,
  home_no: String,
  gender: String,
  address: String,
  qualifications: [{type: Schema.Types.ObjectId, ref: 'student_qualifications'}],
  billings: [{type: Schema.Types.ObjectId, ref: 'billings'}],
  classes: [{type: Schema.Types.ObjectId, ref: 'classes'}],
  image: String,
  created_at: {type: Date, default: Date.now},
  modified_at: Date,
  deleted: {type: Boolean, default: false},
});

//student qualifications db schema
const StudentQualificationSchema = new Schema({
  student: {type: Schema.Types.ObjectId, ref: 'students' },
  type: String, //violin,music theory chinese, english, japanese
  level: Number,
  created_at: {type: Date, default: Date.now},
  modified_at: Date,
  deleted: {type: Boolean, default: false},
});

const Student = mongoose.model('students', StudentSchema);
const StudentQualification = mongoose.model('student_qualifications', StudentQualificationSchema);

module.exports = class StudentClass {
  constructor(){

  }

  getModel(){
    return Student;
  }

  getStudentClass(socket, token, student_id){
    Student.find({student_id:student_id}).populate({path:'classes', match:{$or:[{deleted:null},{deleted:false}]}}).exec((err,students)=>{
      if (err){
        socket.emit("getStudentClass", {result: false, error_message: error_message.pls_try_again_later});
      } else if (students.length != 1) {
        socket.emit("getStudentClass", {result: false, error_message: error_message.student_not_exist});
      } else {
        const s = students[0];
        socket.emit('getStudentClass', {result: true, classes: s.classes});
      }
    });
  }

  uploadStudentProfileImage(socket, token, data){
    const {image, student_id} = data;
    Student.find({student_id: student_id}).exec((err, students)=>{
      if (err || students.length != 1){
        socket.emit('uploadStudentProfileImage', {result: false});
      } else {
        const student = students[0];
        student.image = image;
        student.save((err)=>{
          socket.emit('uploadStudentProfileImage', {result: true});
          //refreseh student list
          socket.emit('reloadStudentList', {result: true});
        })
      }
    });
  }

  addBilling(socket, student, _class, emmitName){

    const BillingClass = require('./billing');
    const Billing = new BillingClass();
    const BillingModel = Billing.getModel();

    if (emmitName == undefined || emmitName == null){
      emmitName = 'joinClass';
    }

    BillingModel.find({student:student, class:_class}).populate('class').exec((err, bs)=>{
      if (err){
        socket.emit(emmitName, {result: false, error_message: error_message.pls_try_again_later});
      } else if (bs.length > 0){
        socket.emit(emmitName, {result: false, error_message: error_message.add_billing_fail});
      } else {
        const billing = new BillingModel();
        billing.student = student;
        billing.class = _class;
        billing.save((err, new_billing)=>{
          if (err){
            socket.emit(emmitName, {result: false, error_message: error_message.pls_try_again_later});
          } else {
            Student.find({_id:student}).exec((err, ss)=>{
              if (err){
                socket.emit(emmitName, {result: false, error_message: error_message.pls_try_again_later});
              } else if (ss.length != 1){
                socket.emit(emmitName, {result: false, error_message: error_message.student_not_exist});
              } else {
                const s = ss[0];
                s.billings.push(new_billing);
                new_billing.populate('class');
                s.save((err, new_student)=>{
                  if (err){
                    socket.emit(emmitName, {result: false, error_message: error_message.pls_try_again_later});
                  } else {
                    //console.log(new_billing.class);
                    socket.emit(emmitName, {result: true, student: s, class: new_billing.class});
                  }
                });
              }
            });
          }
        });
      }
    });
  }

  getStudentQualifications(student_id){
    Student.find({student_id: student_id}).populate('student_qualifications').exec((err,qs)=>{
      return qs;
    });
  }

  getStudents(socket, token, data){
    const {query} = data;

    let sub_condition = [];

    if (query != undefined && query != ''){
      sub_condition.push({$or:[{deleted:null}, {deleted:false}]});

      const split_query = query.split(" ");
      split_query.forEach((value)=>{
        sub_condition.push({ $or: [{student_id: {$regex:value, $options:"i"}},
        {hkid: {$regex:value, $options:"i"}},
        {lastname: {$regex:value, $options:"i"}},
        {firstname: {$regex:value, $options:"i"}}]
        });
      });
    }

    Student.find({$and: sub_condition}).populate('qualifications').exec((err, students)=>{
      if (err){
        socket.emit('getStudents', {result: false});
      } else {
        socket.emit('getStudents', {result: true, students: students});
      }
    });
  }

  getStudentByStudentId(socket, token, student_id){
    Student.find({student_id: student_id}).populate('qualifications').exec((err, students)=>{
      if (err || students.length != 1){
        socket.emit('getStudentByStudentId', {result: false});
      } else {
        console.log(students[0]);
        socket.emit('getStudentByStudentId', {result: true, student: students[0]});
      }
    });
  }

  editStudent(socket, token, student_id, data){
    const {email, lastname, firstname, mobile_no, home_no,
        gender, address, hkid, qualifications} = data;

      // console.log(qualifications);
    Student.find({student_id: student_id}).populate('qualifications').exec((err,students)=>{
      if (err || students.length != 1){
        socket.emit('editStudent', {result: false});
      } else {

        const student = students[0];
        if (email) student.email = email;
        if (address) student.address = address;
        if (lastname) student.lastname = lastname;
        if (firstname) student.firstname = firstname;
        if (mobile_no) student.mobile_no = mobile_no;
        if (home_no) student.home_no = home_no;
        if (gender) student.gender = gender;
        if (hkid) student.hkid = hkid;

        //delete relationship with student_qualifications on students on db
        if (qualifications) student.qualifications = [];

        //find qualifications on student_qualification db to delete
        student.save((err, s)=>{
          if (err) {
            socket.emit('editStudent', {result: false});
          } else if (qualifications && qualifications.length > 0) {
            StudentQualification.find({student: student}).remove().exec((err)=>{
              if (qualifications && qualifications.length > 0){
                //console.log(qualifications.length);
                qualifications.forEach((q)=>{
                  //console.log(i);
                  const sq = new StudentQualification();
                  sq.type = q.type;
                  sq.level = q.level;
                  sq.student = student;
                  sq.save((err,new_q)=>{
                    Student.find({_id: new_q.student}).exec((err, ss)=>{
                      if (!err && ss.length > 0){
                        const s = ss[0];
                        s.qualifications.push(new_q);
                        s.save((err, new_s)=>{
                          socket.emit('editStudent', {result: true});
                          socket.emit('reloadStudentList', {result: true});
                        });
                      }
                    });
                  });
                });
              }
            });
          } else {
              socket.emit('editStudent', {result: true});
              socket.emit('reloadStudentList', {result: true});
          }
        });
      }
    });
  }

  addStudent(socket, token, data){
    const {email,lastname,firstname,mobile_no,home_no,
        gender,address, hkid, student_id, image, qualifications} = data;
    Student.find({$or:[{student_id: student_id},{hkid: hkid}]}).populate('qualifications').exec((err,students)=>{
      if (err ){
        socket.emit('addStudent', {result: false, error_message: error_message.pls_try_again_later});
      } else if (students.length > 0){
        socket.emit('addStudent', {result: false, error_message: error_message.student_exist});
      } else {

        const student = new Student();
        if (email) student.email = email;
        if (address) student.address = address;
        if (lastname) student.lastname = lastname;
        if (firstname) student.firstname = firstname;
        if (mobile_no) student.mobile_no = mobile_no;
        if (home_no) student.home_no = home_no;
        if (gender) student.gender = gender;
        if (hkid) student.hkid = hkid;
        if (student_id) student.student_id = student_id;
        if (image) student.image = image;

        student.save((err, new_student)=>{
          if (err){
            socket.emit('addStudent', {result: false, error_message: error_message.pls_try_again_later});
          } else {

            if (qualifications && qualifications.length > 0){
              qualifications.forEach((q)=>{
                const sq = new StudentQualification();
                sq.type = q.type;
                sq.level = q.level;
                sq.student = new_student._id;
                sq.save((err)=>{
                  if (err){
                    socket.emit('addStudent', {result: false, error_message: error_message.pls_try_again_later});
                  } else {
                    socket.emit('addStudent', {result: true});
                  }
                })
              });
            } else {
              socket.emit('addStudent', {result: true});
            }

          }
        });

      }
    });

  }

  deleteStudent(socket, token, student_id){
    Student.find({student_id:student_id}).exec((err,students)=>{
      if (err || students.length != 1){
        socket.emit('deleteStudent', {result: false});
      } else {
        const student = students[0];
        student.deleted = true;
        student.save((err)=>{
          if (err){
            socket.emit('deleteStudent', {result: false});
          } else {
            socket.emit('deleteStudent', {result: true});
            socket.emit('reloadStudentList', {result: true});
          }
        });
      }
    });
  }

}
