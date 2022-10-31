const error_message = require('../variables/error_message');
const error_code = require('../variables/error_code');
const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const Status = {
  pending: "pending",
  confirmed: "confirmed",
  cancel: "cancel",
}

const BillingSchema = new Schema({
  //billing_id: {type: String, required: true, index: {unique: true} },
  student: {type: Schema.Types.ObjectId, ref: 'students'},
  class: {type: Schema.Types.ObjectId, ref: 'classes'},
  status: {type:String, default: Status.pending}, //pending, confirmed, cancel
  image: String,
  deleted: {type: Boolean, default: false},
  created_at: {type: Date, default: Date.now},
  modified_at: Date,
});

const SMSLogSchema = new Schema({
  //billing_id: {type: String, required: true, index: {unique: true} },
  teacher: {type: Schema.Types.ObjectId, ref: 'users'},
  billings: [{type: Schema.Types.ObjectId, ref: 'billings'}],
  contents: [{type: String}],
  deleted: {type: Boolean, default: false},
  created_at: {type: Date, default: Date.now},
  modified_at: Date,
});

const SMSLog = mongoose.model('smslogs', SMSLogSchema);
const Billing = mongoose.model('billings', BillingSchema);

module.exports = class BillingClass {
  constructor(){
  }

  getModel(){
    return Billing;
  }

  getSMSLog(socket, token, data){
    const UserClass = require('./user');
    const User = new UserClass();
    const UserModel = User.getModel();

    UserModel.find({token: token, deleted: {$in: [null, false]}}).exec((err, users)=>{
        // console.log(users);
      if (err){
        console.log(err);
        socket.emit('getSMSLog', {result: false, error_message: error_message.pls_try_again_later});
      } else if (users.length != 1){
        socket.emit('getSMSLog', {result: false, error_message: error_message.data_wrong});
      } else {
        const u = users[0];
        SMSLog.find({teacher: u._id, deleted: {$in: [null, false]}}).populate({path:'billings', populate: [{path:'class'},{path:'student'}] }).exec((err, smss)=>{
          // console.log(smss);
          if (err){
            console.log(err);
            socket.emit('getSMSLog', {result: false, error_message: error_message.pls_try_again_later});
          } else if (smss.length < 1){
            socket.emit('getSMSLog', {result: false, error_message: error_message.empty_result});
          } else {
            socket.emit('getSMSLog', {result: true, smss: smss});
          }
        });
      }
    });
  }

  uploadSMSLog(socket, token, data){
    const UserClass = require('./user');
    const User = new UserClass();
    const UserModel = User.getModel();

    const {billings, contents} = data;
    UserModel.find({token: token, deleted: {$in: [null, false]}}).exec((err,users)=>{
      if (err){
        console.log(err);
        socket.emit('uploadSMSLog', {result: false, error_message: error_message.pls_try_again_later});
      } else if (users.length != 1){
        socket.emit('uploadSMSLog', {result: false, error_message: error_message.data_wrong});
      } else {
        const u = users[0];
        const smsLog = new SMSLog();
        smsLog.teacher = u;
        smsLog.billings = billings;
        smsLog.contents = contents;
        smsLog.save((err, new_smsLog)=>{
          if (err){
            console.log(err);
            socket.emit('uploadSMSLog', {result: false, error_message: error_message.pls_try_again_later});
          } else {
            socket.emit('uploadSMSLog', {result: true});
          }
        })
        // socket.emit('uploadSMSLog', {result: true, billing: b});
      }
    });
  }

  updateBilling(socket, token, data){
    const {image, _id, status} = data;
    Billing.find({_id: _id, deleted: {$in: [null, false]}})
    .populate({path:'student', match: {deleted: {$in: [null,false]}}})
    .populate({path:'class', match: {deleted: {$in: [null,false]}}})
    .exec((err, billings)=>{
      if (err){
        console.log(err);
        socket.emit('updateBilling', {result: false, error_message: error_message.pls_try_again_later});
      } else if (billings.length != 1){
        socket.emit('updateBilling', {result: false, error_message: error_message.data_wrong});
      } else {
        const b = billings[0];
        if (image != null){
          b.image = image;
        }
        //for sms
        const original_status = b.status;
        b.status = status;
        b.save((err)=>{
          if (err){
            console.log(err);
            socket.emit('updateBilling', {result: false, error_message: error_message.pls_try_again_later});
          } else {
            socket.emit('updateBilling', {result: true});

            if (original_status != Status.confirmed && b.status == Status.confirmed){
              socket.emit('sendConfirmedEmail', {result: true, student: b.student, class: b.class});
            } else if (original_status != Status.cancel && b.status == Status.cancel){
              socket.emit('sendCancelEmail', {result: true, student: b.student, class: b.class});
            } else if (original_status != Status.pending && b.status == Status.pending){
              socket.emit('sendPendingEmail', {result: true, student: b.student, class: b.class});
            } else {
              socket.emit('reloadBillings', {result: true});
            }

          }
        });

      }
    });
  }

  getBilling(socket, token, data){
    const {_id} = data;
    Billing.find({_id: _id, deleted: {$in: [null, false]}})
    .populate({path:'student', match: {deleted: {$in: [null,false]}}})
    .populate({path:'class', match: {deleted: {$in: [null,false]}}})
    .exec((err, billings)=>{
      if (err){
        console.log(err);
        socket.emit('getBilling', {result: false, error_message: error_message.pls_try_again_later});
      } else if (billings.length != 1){
        socket.emit('getBilling', {result: false, error_message: error_message.data_wrong});
      } else {
        const b = billings[0];

          if (b.student != null && b.class != null){
            socket.emit('getBilling', {result: true, billing: b});
          } else {

              socket.emit('getBilling', {result: false, error_message: error_message.data_wrong});
          }


      }
    });
  }

  getBillings(socket, token, data){

    const _ClassClass = require('./class');
    const _Class = new _ClassClass();
    const _ClassModel = _Class.getModel();

    const {class_id, student_id} = data;

    if (class_id != undefined && class_id != null){
      _ClassModel.find({class_id: class_id, deleted: {$in: [null,false]}})
      .exec((err, classes)=>{
        if (err){
          console.log(err);
          socket.emit('getBillings', {result: false, error_message: error_message.pls_try_again_later});
        } else if (classes.length != 1){
          socket.emit('getBillings', {result: false, error_message: error_message.data_wrong});
        } else {
          //console.log("test");
          const c = classes[0];
          Billing.find({class: c, deleted: {$in: [null, false]}})
          .populate({path:'student', match:{deleted:{$in: [null,false]}}})
          .populate({path:'class', match:{deleted:{$in: [null,false]}}})
          .exec((err,billings)=>{
            if (err){
              console.log(err);
              socket.emit('getBillings', {result: false, error_message: error_message.pls_try_again_later});
            } else if (billings.length < 1){
              socket.emit('getBillings', {result: false, error_message: error_message.billing_not_exist});
            } else {

              let b_array = [];
              billings.forEach((b)=>{
                if (b.student != null && b.class != null){
                  b_array.push(b);
                }
              });
              //console.log(billings);
              socket.emit('getBillings', {result: true, billings: b_array});
            }

          });
        }
      });

    } else if (student_id != undefined && student_id != null){

      const StudentClass = require('./student');
      const Student = new StudentClass();
      const StudentModel = Student.getModel();

      StudentModel.find({student_id: student_id, deleted: {$in: [null, false]}})
        .populate({path:'billings', match: {deleted: {$in: [null, false]}}})
        .populate({path:'classes', match: {deleted: {$in: [null, false]}}})
        .exec((err,students)=>{
          if (err){
            console.log(err);
            socket.emit('getBillings', {result: false, error_message: error_message.pls_try_again_later});
          } else if (students.length != 1){
            socket.emit('getBillings', {result: false, error_message: error_message.data_wrong});
          } else {
            //console.log("test");
            const s = students[0];
            Billing.find({student: s, deleted: {$in: [null, false]}})
            .populate({path:'student', match:{deleted:{$in: [null,false]}}})
            .populate({path:'class', match:{deleted:{$in: [null,false]}}})
            .exec((err,billings)=>{
              if (err){
                console.log(err);
                socket.emit('getBillings', {result: false, error_message: error_message.pls_try_again_later});
              } else if (billings.length < 1){
                socket.emit('getBillings', {result: false, error_message: error_message.billing_not_exist});
              } else {
                //console.log(billings);
                socket.emit('getBillings', {result: true, billings: billings});
              }

            });
          }
        });
    }
  }
}
