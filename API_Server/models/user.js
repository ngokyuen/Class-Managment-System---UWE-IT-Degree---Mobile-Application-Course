const error_message = require('../variables/error_message');
const error_code = require('../variables/error_code');

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

//user db schema
const UserSchema = new Schema({
  email: {type: String, required: true, index: {unique: true}},
  password: {type: String, required: true},
  hkid: String,
  lastname: String, firstname: String,
  mobile_no: String, home_no: String,
  gender: String, address: String,
  token: {type: String, index: {unique: true} },
  image: String,
  classes: [{type: Schema.Types.ObjectId, ref: 'classes'}],
  created_at: {type: Date, default: Date.now},
  modified_at: Date,
  deleted: {type: Boolean, default: false},
});

const User = mongoose.model('users', UserSchema);

module.exports = class UserClass {
  constructor(){

  }

  getModel(){
    return User;
  }

  getUserByName(socket, token, data){
    const {query} = data;
    const query_arr = query.split(" ");
    User.find({$or: [{lastname: {$in: query_arr}}, {firstname: {$in: query_arr}}]}).exec((err, users)=>{
      if (err){
        console.log(err);
        socket.emit('getUserByName', {result: false});
      } else {
        console.log( {result: true, users: users});
        socket.emit('getUserByName', {result: true, users: users});
      }
    })
  }

  uploadProfileImage(socket, token, data){
    const {image} = data;
    User.find({token: token}).exec((err, users)=>{
      if (err || users.length != 1){
        socket.emit('uploadProfileImage', {result: false});
      } else {
        const user = users[0];
        user.image = image;
        user.save((err)=>{
          socket.emit('uploadProfileImage', {result: true});
          socket.emit('getProfile2', {result: true, user: user});
        })
      }
    });
  }

  registerAccount(socket, email, password){
    User.find({email: email}).exec((err, users)=>{
      if (err){
        console.log(err);
        socket.emit('registerAccount', {result: false, error_message: error_message.pls_try_again_later});
      } else if (users.length > 0) {
        socket.emit('registerAccount', {result: false, error_message: error_message.account_existing});
      } else {
        const temp_user = new User();
        temp_user.email = email;
        temp_user.password = password;
        temp_user.token = email;
        temp_user.save((err2,new_user)=>{
          if (err2){
            console.log(err2);
            socket.emit('registerAccount', {result: false, error_message: error_message.pls_try_again_later});
          } else {
            socket.emit('registerAccount', {result: true});
          }
        });

      }
    });
  }

  verifyLogin(socket, email, password){
    User.find({email: email, password: password}).exec((err, users)=>{
      if (err){
        console.log(err);
          socket.emit('login', {result: false, error_message: error_message.pls_try_again_later});
      } else if (users.length != 1) {
        User.find({email: email}).exec((err2, users2)=>{
          if (err2){
            console.log(err2);
            socket.emit('login', {result: false, error_message: error_message.pls_try_again_later});
          } else if (users2.length > 0) {

              socket.emit('login', {result: false, error_message: error_message.wrong_password});
          } else {

              socket.emit('login', {result: false, error_message: error_message.user_not_existing});
          }
        });

        // if (users.length < 1){
        //   const user = new User();
        //   user.email = email;
        //   user.password = password;
        //   user.token = email;
        //   user.save();
        //   socket.emit('register', {result: true});
        //   socket.emit('login', {result: true, token: email});
        // } else {
        // }
      } else {
        const temp_user = users[0];
        delete temp_user.password;
        socket.emit('login', {result: true, user:temp_user});
      }
    });
  }

  verifyLoginToken(socket, token){
    User.find({token: token}).exec((err, users)=>{
      if (err || users.length != 1){
        socket.emit('login', {result: false});
      } else {
        const temp_user = users[0];
        delete temp_user.password;
        socket.emit('login', {result: true, user:temp_user});
      }
    });
  }

  getProfile(socket, token){
    User.find({token: token}).exec((err, users)=>{
      if (err || users.length != 1){
        socket.emit('getProfile', {result: false});
      } else {
        const user = users[0];
        delete user.password;
        socket.emit('getProfile', {result: true, user: user});
        socket.emit('getProfile2', {result: true, user: user});
      }
    });
  }

  updateProfile(socket, data){
    const {email,password,lastname,firstname,mobile_no,home_no,
        gender,address,token, hkid} = data;
    User.find({token: token}).exec((err,users)=>{
      if (err || users.length != 1){
        socket.emit('updateProfile', {result: false});
      } else {
        const user = users[0];
        if (password) user.password = password;
        if (email) user.email = email;
        if (address) user.address = address;
        if(lastname) user.lastname = lastname;
        if (firstname) user.firstname = firstname;
        if (mobile_no) user.mobile_no = mobile_no;
        if (home_no) user.home_no = home_no;
        if (gender) user.gender = gender;
        if (hkid) user.hkid = hkid;
        user.save();

        const temp_user = user;
        delete temp_user.password;
        socket.emit('updateProfile', {result: true, user: temp_user});
        socket.emit('getProfile', {result: true, user: temp_user});
        socket.emit('getProfile2', {result: true, user: temp_user});
      }
    })

  }
}
