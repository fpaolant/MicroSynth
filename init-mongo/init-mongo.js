db = db.getSiblingDB('microsynth');

db.createCollection('users');

db.users.insertMany([
    { firstname: "Fabio", lastname:"Paolantonio", username:"fpaolant", password:"12345", email: "fabio@example.com" },
    { firstname: "Paolo", lastname: "Rossi", username:"prossi", password:"12345", email: "paolo@example.com" }
]);
