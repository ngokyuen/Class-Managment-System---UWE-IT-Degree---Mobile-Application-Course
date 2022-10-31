const error_message = require('../variables/error_message');
const error_code = require('../variables/error_code');
const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const Status = {
  open: "open", cancel: "cancel", //full: "full",
  beginning: "beginning", completed: "completed" ,
}
const Type = {
  individual: "individual", small: "small",
}

const ClassSchema = new Schema({
  class_id: {type: String, required: true, index: {unique: true} },
  name: String,address: String,
  longitude: Number,latitude: Number,
  price: Number,
  type: String, //inividule, small class
  min_level_type: String, //violin,music theory chinese, english, japanese
  min_level: Number, //1-10
  min_level_type2: String,min_level2: Number,
  min_level_type3: String,min_level3: Number,
  max_students: {type:Number, default: 1},
  status: {type:String, default: Status.open }, //type: open, cancel, beginning, completed
  teachers: [{type: Schema.Types.ObjectId, ref:'users'}],
  students: [{type: Schema.Types.ObjectId, ref:'students'}],
  attachments: [{type: Schema.Types.ObjectId, ref:'attachments'}],
  // class_schedules: [{type: Schema.Types.ObjectId, ref:'class_schedules'}],
  weekly: {type:Boolean, default: false},
  start_date: Date,
  end_date: Date,
  start_time: Number,
  end_time: Number,
  // hours: Number,
  no_of_lession: Number,
  deleted: {type: Boolean, default: false},
  created_at: {type: Date, default: Date.now}, modified_at: Date,
});

const ClassScheduleSchema = new Schema({
  class: {type: Schema.Types.ObjectId, ref:'classes'},
  start_date: Date,
  // end_date: Date,
  start_time: Number,
  end_time: Number,
  deleted: {type: Boolean, default: false},
  created_at: {type: Date, default: Date.now},
  modified_at: Date,
});

const _Class = mongoose.model('classes', ClassSchema);
const _ClassSchedule = mongoose.model('class_schedules', ClassScheduleSchema);

module.exports = class _ClassClass {
  constructor(){

  }

  getModel(){
    return _Class;
  }

  uploadClassSchedule(socket, token, data){
    const {class_id, start_time, end_time, start_date} = data;
    _Class.find({class_id: class_id, deleted: {$in: [null,false]}}).exec((err, classes)=>{
      if (err){
        console.log(err);
        socket.emit('uploadClassSchedule', {result: false});
      } else {
        const c = classes[0];
        const {no_of_lession} = c;
        _ClassSchedule.find({class: c._id, deleted: {$in:[null,false]}}).exec((err, schedules)=>{

          if (schedules.length >= no_of_lession){
            //console.log( {result: false, error_message: error_message.full_class_schedule});
            socket.emit('uploadClassSchedule', {result: false, error_message: error_message.full_class_schedule});
          } else {

            let verify_existing_result = true;
            schedules.forEach((s)=>{
              //console.log(s.start_date);
              //console.log( new Date(start_date));
              if (s.start_date.getTime() === (new Date(start_date)).getTime()){
                verify_existing_result = false;
              }
            });

            if (!verify_existing_result){
              socket.emit('uploadClassSchedule', {result: false, error_message: error_message.duplicate_class_schedule});
            } else {
              const _classSchedule = new _ClassSchedule();
              _classSchedule.class = c._id;
              _classSchedule.start_time = start_time;
              _classSchedule.end_time = end_time;
              _classSchedule.start_date = start_date;
              _classSchedule.save((err)=>{
                if (err){
                  console.log(err);
                  socket.emit('uploadClassSchedule', {result: false});
                } else {
                  socket.emit('uploadClassSchedule', {result: true});
                }
              });

            }
          }

        });

      }
    });
  }

  deleteClassSchedule(socket, token, data){
    const {_id} = data;
    _ClassSchedule.find({_id: _id, deleted:{$in: [null,false]}}).exec((err, css)=>{
      if (err){
        console.log(err);
        socket.emit('deleteClassSchedule', {result: false});
      } else {
        const cs = css[0];
        cs.deleted = true;
        cs.save((err)=>{
          if (err){
            console.log(err);
            socket.emit('deleteClassSchedule', {result: false});
          } else {
            socket.emit('deleteClassSchedule', {result: true});
          }
        });

      }
    });
  }

  getDayClassSchedule(socket, token, data){
    const {year, month, day, myself} = data;
    //console.log((new Date(year,month,day)).toISOString());
    _ClassSchedule.find({start_date: (new Date(year,month,day)).toISOString(), deleted:{$in:[null,false]}})
    .populate({path:'class', match:{deleted: {$in:[null, false]}}, populate: {path:'teachers', match: {deleted: {$in: [false,null]}}}})
    .exec((err,css)=>{
      if (err){
        console.log(err);
        socket.emit('getDayClassSchedule', {result: false});
      } else {

        if (myself != null && myself){
          const UserClass = require('./user');
          const User = new UserClass();
          const UserModel = User.getModel();
          UserModel.find({token:token}).exec((err,users)=>{
            if (err){
              console.log(err);
              socket.emit('getDayClassSchedule', {result:false});
            } else {
              const user = users[0];
              const user_id = user._id;
              let new_css = [];
              css.forEach((cs)=>{
                cs.class.teachers.forEach((t)=>{
                  if (t._id.toString() === user_id.toString()){
                    new_css.push(cs);
                  }
                });
              });
              socket.emit('getDayClassSchedule', {result:true, class_schedules: new_css});
            }
          });


        } else {
          socket.emit('getDayClassSchedule', {result:true, class_schedules: css});
        }

      }
    });
  }

  getMonthClassSchedule(socket, token, data){
    const {year, month, myself} = data;
    _ClassSchedule.find({start_date: {$lte: (new Date(year,month+3,31)).toISOString(), $gte: (new Date(year, month-3, 1)).toISOString()} , deleted:{$in:[null,false]}})
      .populate({path:'class', match:{deleted: {$in:[null, false]}}, populate: {path:'teachers', match: {deleted: {$in: [false,null]}}}})
      .exec((err,css)=>{
      if (err){
        console.log(err);
        socket.emit('getMonthClassSchedule', {result: false});
      } else {

        if (myself != null && myself){
          const UserClass = require('./user');
          const User = new UserClass();
          const UserModel = User.getModel();
          UserModel.find({token:token}).exec((err,users)=>{
            if (err){
              console.log(err);
              socket.emit('getMonthClassSchedule', {result:false});
            } else {
              const user = users[0];
              const user_id = user._id;
              let new_css = [];
              css.forEach((cs)=>{
                cs.class.teachers.forEach((t)=>{
                  if (t._id.toString() === user_id.toString()){
                    new_css.push(cs);
                  }
                });
              });
              socket.emit('getMonthClassSchedule', {result:true, class_schedules: new_css});
            }
          });

        } else {
          socket.emit('getMonthClassSchedule', {result:true, class_schedules: css});
        }

      }
    });
  }

  // addClassSchedule(socket, token, data){
  //   const {_id, start_date, end_date, start_time, end_time} = data;
  //
  //   const classSchedule = new _ClassSchedule();
  //   classSchedule.class = _id;
  //   classSchedule.start_date = start_date;
  //   classSchedule.end_date = end_date;
  //   classSchedule.start_time = start_time;
  //   classSchedule.end_time = end_time;
  //   classSchedule.save((err)=>{
  //     socket.emit('addClassSchedule', {result: true});
  //   });
  // }

  recommendClass(socket, token, student_id){
    const StudentClass = require('./student');
    const Student = new StudentClass();
    const StudentModel = Student.getModel();

    StudentModel.find({student_id: student_id, deleted: {$in: [null,false]}}).populate('qualifications').exec((err, students)=>{
      if (err){
        console.log(err);
        socket.emit('recommendClass', {result: false});
      } else if (students.length != 1){
        socket.emit('recommendClass', {result: false, error_message: error_message.data_wrong});
      } else {

        const s = students[0];
        const qs = [];
        if (s.qualifications != undefined && s.qualifications != null){
          const qs = s.qualifications;
        }

        const condi = [];
        condi.push({deleted: {$in:[false, null]}})

        let min_level_condi = [];
        let min_level_condi2 = [];
        let min_level_condi3 = [];

        min_level_condi.push({min_level_type: null});
        min_level_condi2.push({min_level_type2: null});
        min_level_condi3.push({min_level_type3: null});
        qs.forEach((q)=>{
          const {type, level} = q;
          min_level_condi.push({min_level_type: type, min_level: {$lte: level}});
          min_level_condi2.push({min_level_type2: type, min_level2: {$lte: level}});
          min_level_condi3.push({min_level_type3: type, min_level3: {$lte: level}});
        });
        condi.push({$or: min_level_condi});
        condi.push({$or: min_level_condi2});
        condi.push({$or: min_level_condi3});

        _Class.find({$and: condi})
          .populate({path:'students', match:{deleted: {$in:[null, false]}}})
          .populate({path:'teachers', match:{deleted: {$in:[null, false]}}})
          .exec((err, classes)=>{
          if (err){
            console.log(err);
            socket.emit('recommendClass', {result: false, error_message: error_message.pls_try_again_later});
          } else if ( classes.length < 1){
            socket.emit('recommendClass', {result: false, error_message: error_message.class_not_exist});
          } else {
            socket.emit('recommendClass', {result:true, classes: classes});
          }
        });

      }
    });
  }

  joinClass(socket, token, data, emmitName){

    const StudentClass = require('./student');
    const Student = new StudentClass();
    const StudentModel = Student.getModel();

    if (emmitName == undefined || emmitName == null){
      emmitName = 'joinClass';
    }

    const {class_id, student_id} = data;

    StudentModel.find({student_id: student_id, deleted: {$in: [null,false]}}).populate('qualifications').exec((err, students)=>{
      if (err){
        console.log(err);
        socket.emit(emmitName, {result: false, error_message: error_message.pls_try_again_later});
      } else if (students.length != 1) {
        socket.emit(emmitName, {result: false, error_message: error_message.data_wrong});
      } else {

        const s = students[0];
        const qs = s.qualifications;
        const condi = [];
        condi.push({deleted: {$in: [null,false]}});

        let min_level_condi = [];
        let min_level_condi2 = [];
        let min_level_condi3 = [];

        min_level_condi.push({min_level_type: null});
        min_level_condi2.push({min_level_type2: null});
        min_level_condi3.push({min_level_type3: null});
        qs.forEach((q)=>{
          const {type, level} = q;
          min_level_condi.push({min_level_type: type, min_level: {$lte: level}});
          min_level_condi2.push({min_level_type2: type, min_level2: {$lte: level}});
          min_level_condi3.push({min_level_type3: type, min_level3: {$lte: level}});
        });
        condi.push({$or: min_level_condi});
        condi.push({$or: min_level_condi2});
        condi.push({$or: min_level_condi3});
        condi.push({class_id: class_id});

        _Class.find({$and: condi})
          .populate({path:'students', match:{deleted: {$in:[null, false]}}})
          .exec((err, classes)=>{
          if (err) {
            console.log(err);
            socket.emit(emmitName, {result: false, error_message: error_message.pls_try_again_later});
          } else if (classes.length > 1){
            socket.emit(emmitName, {result: false, error_message: error_message.data_wrong});
          } else if (classes.length != 1){
            socket.emit(emmitName, {result: false, error_code: error_code.level_not_enough, error_message: error_message.level_not_enough});
          } else {

            const c = classes[0];
            let result = true;
            c.students.forEach((class_student)=>{
              if (class_student.student_id == s.student_id){
                result = false;
              }
            });

            if (result){
              console.log(result);
              c.students.push(s);
              c.save((err)=>{
                if (err){
                  console.log(err);
                  socket.emit(emmitName, {result: false, error_message: error_message.pls_try_again_later});
                } else {
                  //Student.addClass(socket, s, c, emmitName);
                  s.classes.push(c);
                  s.save((err)=>{
                    Student.addBilling(socket, s, c, emmitName);
                  });
                }
              });
            } else {
              socket.emit(emmitName, {result: false, error_message: error_message.has_joined_class});
            }
          }
        });
      }
    });
  }

  getClass(socket, token, class_id){

    _Class.find({class_id: class_id, deleted: {$in: [null,false]}})
    .populate({path:'teachers', match:{deleted:{$in: [null,false]}}})
    .exec((err, _classes)=>{
      if (err && _classes.length != 1){
        socket.emit('getClass', {result: false});
      } else {
        socket.emit('getClass', {result: true, class: _classes[0]});
      }
    });
  }

  searchClasses(socket, token, data){
    let {query, price_from, price_to, status, type, date_from, date_to, time_from, time_to,
      min_level_type, min_level_from, min_level_to,
      min_level_type2, min_level2_from,min_level2_to,
      min_level_type3, min_level3_from, min_level3_to} = data;
    let sub_condition = [];

    if (query != undefined && query != ''){
      const split_query = query.split(" ");
      split_query.forEach((value)=>{
        sub_condition.push({ $or: [{class_id: {$regex: value, $options: "i"}}, {name: {$regex:value, $options: "i"}}, {address: {$regex:value, $options: "i"}}] });
      });
    }

    if (date_from != undefined && date_from !='' && date_to != undefined && date_to !=''){
      const temp_date_from = new Date(date_from);
      const temp_date_to = new Date(date_to);
      sub_condition.push({$or:[
        {start_date: {$lte: temp_date_from}, end_date: {$gte: temp_date_from}},
        {start_date: {$lte: temp_date_to}, end_date: {$gte: temp_date_to}}
      ]});
    } else if (date_from != undefined && date_from !=''){
      const temp_date_from = new Date(date_from);
      sub_condition.push({start_date: {$lte: temp_date_from}, end_date: {$gte: temp_date_from}});
    } else if (date_to != undefined && date_to !=''){
        const temp_date_to = new Date(date_to);
        sub_condition.push({start_date: {$lte: temp_date_to}, end_date: {$gte: temp_date_to}});
    }


    if (time_from != undefined && time_from !='' && time_to != undefined && time_to !=''){
      sub_condition.push({$or:[
        {start_time: {$lte: time_from}, end_time: {$gte: time_from}},
        {start_time: {$lte: time_to}, end_time: {$gte: time_to}}
      ]});
    } else if (time_from != undefined && time_from !=''){
      sub_condition.push({ start_time: {$lte: time_from}, end_time: {$gte: time_from}  });
    } else if (time_to != undefined && time_to !=''){
      sub_condition.push({ start_time: {$lte: time_to}, end_time: {$gte: time_to}  });
    }

    if (price_from != undefined && price_from !=''){
      sub_condition.push({ price: {$gte: price_from} });
    }


    if (price_to != undefined && price_to !=''){
      sub_condition.push({ price: {$lte: price_to} });
    }

     if (status != undefined && status !=''){
      //  console.log(status);
       const split_status = status.split(" ");
       sub_condition.push({status: {$in:split_status} });
     }

    if (type != undefined && type != ''){
      const split_type = type.split(" ");
      sub_condition.push({type: {$in:split_type} });
    }

    if (min_level_type != '' && (min_level_from != '' || min_level_to != '')){
      if (min_level_from == ''){
        min_level_from = 0;
      }
      if (min_level_to == ''){
        min_level_to = 9999;
      }
      sub_condition.push({ $or: [
        {$and: [{min_level: {$lte: min_level_to, $gte: min_level_from}},{min_level_type: min_level_type}]},
        {$and: [{min_level2: {$lte: min_level_to, $gte: min_level_from}},{min_level_type2: min_level_type}]},
        {$and: [{min_level3: {$lte: min_level_to, $gte: min_level_from}},{min_level_type3: min_level_type}]},
      ]});
    }

    if (min_level_type2 != ''  && (min_level2_from != '' || min_level2_to != '')){
      if (min_level2_from == ''){
        min_level2_from = 0;
      }
      if (min_level2_to == ''){
        min_level2_to = 9999;
      }
      sub_condition.push({ $or: [
        {$and: [{min_level: {$lte: min_level2_to, $gte: min_level2_from}},{min_level_type: min_level_type2}]},
        {$and: [{min_level2: {$lte: min_level2_to, $gte: min_level2_from}},{min_level_type2: min_level_type2}]},
        {$and: [{min_level3: {$lte: min_level3_to, $gte: min_level3_from}},{min_level_type3: min_level_type2}]},
      ]});
    }

    if (min_level_type3 != ''  && (min_level3_from != '' || min_level3_to != '')){
      if (min_level3_from == ''){
        min_level3_from = 0;
      }
      if (min_level3_to == ''){
        min_level3_to = 9999;
      }
      sub_condition.push({ $or: [
        {$and: [{min_level: {$lte: min_level3_to, $gte: min_level3_from}},{min_level_type: min_level_type3}]},
        {$and: [{min_level2: {$lte: min_level3_to, $gte: min_level3_from}},{min_level_type2: min_level_type3}]},
        {$and: [{min_level3: {$lte: min_level3_to, $gte: min_level3_from}},{min_level_type3: min_level_type3}]},
      ]});
    }

    let condition = { $and: sub_condition };
    _Class.find(condition)
      .populate({path:'students', match:{deleted: {$in:[null, false]}}})
      .populate({path:'teachers', match:{deleted:{$in: [null,false]}}})
      .exec((err, _classes)=>{
      if (err){
        socket.emit('searchClasses', {result: false});
      } else {
        socket.emit('searchClasses', {result: true, classes: _classes});
      }
    });
  }

  editClass(socket, token, class_id, data){

    const UserClass = require('./user');
    const User = new UserClass();
    const UserModel = User.getModel();

    const {name,address,longitude,latitude,price,type,
      min_level,min_level2,min_level3,
      min_level_type,min_level_type2,min_level_type3,
      max_students, status, teachers,start_date, end_date, start_time, end_time, no_of_lession, weekly } = data;

    UserModel.find({token: token}).exec((err,users)=>{
        if (err || users.length != 1){
          socket.emit('editClass', {result: false});
        } else {

          const user = users[0];
          _Class.find({class_id: class_id}).exec((err,_classes)=>{
            if (err || _classes.length != 1){
              socket.emit('editClass', {result: false});
            } else {

              const _class = _classes[0];
              if (name) _class.name = name;
              if (address) _class.address = address;
              if (longitude) _class.longitude = longitude;
              if (latitude) _class.latitude = latitude;
              if (price) _class.price = price;
              if (status) _class.status = status;
              if (type) _class.type = type;
              if (min_level_type && min_level){
                _class.min_level_type = min_level_type;
                _class.min_level = min_level;
              }
              if (min_level_type2 && min_level2){
                _class.min_level_type2 = min_level_type2;
                _class.min_level2 = min_level2;
              }
              if (min_level_type3 && min_level3){
                _class.min_level_type3 = min_level_type3;
                _class.min_level3 = min_level3;
              }

              if (type == Type.individual){
                _class.max_students = 1;
              } else if (max_students){
                _class.max_students = max_students;
              }

              if (start_date) _class.start_date = start_date;
              if (end_date) _class.end_date = end_date;
              if (start_time) _class.start_time = start_time;
              if (end_time) _class.end_time  = end_time;
              if (no_of_lession) _class.no_of_lession = no_of_lession;
              if (weekly) {
                _class.weekly = true;
              } else {
                _class.weekly = false;
              }

              if (teachers) _class.teachers = teachers;

              _class.save((err, new_class)=>{
                if (err){
                  socket.emit('editClass', {result: false});
                } else {

                  if (weekly) {

                    _ClassSchedule.find({class: new_class}).remove((err)=>{
                      // console.log(no_of_lession);
                      let docs = [];
                      for (var i=0; i< no_of_lession; i++){


                        let curr_start_date = new Date(start_date);
                        let curr_end_date = new Date(end_date);

                        curr_start_date.setTime(curr_start_date.getTime() + i *7 * 86400000  - 28800000);

                        let class_schedule_ = new _ClassSchedule();

                        class_schedule_.start_time = start_time;
                        class_schedule_.end_time = end_time;
                        class_schedule_.start_date = curr_start_date;
                        // class_schedule_.end_date = curr_end_date;
                        class_schedule_.class = new_class;
                        docs.push(class_schedule_);
                        // class_schedule_.save((err2)=>{
                        //   if (err2){
                        //     console.log(err2);
                        //     socket.emit('editClassWeekly', {result: false});
                        //   } else {
                        //     socket.emit('editClassWeekly', {result: true});
                        //     socket.emit('reloadClassList', {result: true});
                        //   }
                        // });
                      }
                      // console.log(docs);

                      _ClassSchedule.insertMany(docs, (err)=>{
                        if (err){
                          console.log(err);
                        } else {
                          socket.emit('editClassWeekly', {result: true});
                          socket.emit('reloadClassList', {result: true});
                        }
                      });

                    });

                  } else {
                    socket.emit('editClass', {result: true});
                    socket.emit('reloadClassList', {result: true});
                  }
                }
              });
            }
          });
        }
      });
  }


  addClass(socket, token, data){

    const UserClass = require('./user');
    const User = new UserClass();
    const UserModel = User.getModel();

    const {class_id,name,address,longitude,latitude,price,type,
      min_level,min_level2,min_level3,
      min_level_type,min_level_type2,min_level_type3,
      max_students, status, teachers,start_date, end_date, start_time, end_time, no_of_lession, weekly} = data;

    UserModel.find({token: token, deleted:{$in:[null,false]}}).exec((err,users)=>{
        if (err || users.length != 1){
          socket.emit('addClass', {result: false});
        } else {

          const user = users[0];
          _Class.find({class_id: class_id, deleted:{$in:[null,false]}}).exec((err,_classes)=>{
          //_Class.find({class_id: {$regex:class_id, $options: "i"}}).exec((err,_classes)=>{
            if (err || _classes.length > 0){
              console.log(err);
              socket.emit('addClass', {result: false, error_message: error_message.same_class_id});
            } else {

              const _class = new _Class();
              if (name) _class.name = name;
              if (address) _class.address = address;
              if (longitude) _class.longitude = longitude;
              if (latitude) _class.latitude = latitude;
              if (price) _class.price = price;
              if (status) _class.status = status;
              if (type) _class.type = type;
              if (min_level_type && min_level){
                _class.min_level_type = min_level_type;
                _class.min_level = min_level;
              }
              if (min_level_type2 && min_level2){
                _class.min_level_type2 = min_level_type2;
                _class.min_level2 = min_level2;
              }
              if (min_level_type3 && min_level3){
                _class.min_level_type3 = min_level_type3;
                _class.min_level3 = min_level3;
              }
              if (max_students) _class.max_students = max_students;
              if (teachers) _class.teachers = teachers;
              if (class_id) _class.class_id = class_id;
              if (start_date) _class.start_date = start_date;
              if (end_date) _class.end_date = end_date;
              if (start_time) _class.start_time = start_time;
              if (end_time) _class.end_time  = end_time;
              // if (hours) _class.hours = hours;
              if (no_of_lession) _class.no_of_lession = no_of_lession;
              if (weekly) {
                _class.weekly = true;
              } else {
                _class.weekly = false;
              }

              //_class.teachers.push(user);

              _class.save((err, new_class)=>{
                if (err){
                  console.log(err);
                  socket.emit('addClass', {result: false});
                } else {

                  if (weekly) {
                    _ClassSchedule.find({class: new_class}).remove((err)=>{
                      // console.log(no_of_lession);
                      let docs = [];
                      for (var i=0; i< no_of_lession; i++){

                        let curr_start_date = new Date(start_date);

                        if (i > 0){
                          curr_start_date.setTime(curr_start_date.getTime() + i *7 * 86400000 );
                          // curr_end_date.setTime(curr_end_date.getTime() + i* 7 * 86400000 );
                        }

                        let class_schedule_ = new _ClassSchedule();

                        class_schedule_.start_time = start_time;
                        class_schedule_.end_time = end_time;
                        class_schedule_.start_date = curr_start_date;
                        // class_schedule_.end_date = curr_end_date;
                        class_schedule_.class = new_class;
                        docs.push(class_schedule_);
                        // class_schedule_.save((err2)=>{
                        //   if (err2){
                        //     console.log(err2);
                        //     socket.emit('editClassWeekly', {result: false});
                        //   } else {
                        //     socket.emit('editClassWeekly', {result: true});
                        //     socket.emit('reloadClassList', {result: true});
                        //   }
                        // });
                      }
                      // console.log(docs);

                      _ClassSchedule.insertMany(docs, (err)=>{
                        if (err){
                          console.log(err);
                        } else {
                          socket.emit('addClassWeekly', {result: true});
                          socket.emit('reloadClassList', {result: true});
                        }
                      });

                    });

                  } else {
                    socket.emit('addClass', {result: true});
                  }

                  // });
                }
              });

            }
          });
        }

      });
    }
}
