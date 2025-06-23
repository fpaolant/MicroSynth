/* mongo-init.js */

db = db.getSiblingDB('microsynth');

/* ================== USERS ================== */
db.createCollection('users');

db.users.insertMany([
    {
        firstname: 'Antonio',
        lastname: 'Bianchi',
        username: 'admin',
        password: '$2a$10$XUFw0JcXbObu5K9T/BwxLOsPPuLrj6x4AYkQ.gi3S56/l1Qgqz/l2',
        email: 'antonio@example.com',
        roles: ['USER', 'ADMIN']
    },
    {
        firstname: 'Paolo',
        lastname: 'Rossi',
        username: 'prossi',
        password: '$2a$10$XUFw0JcXbObu5K9T/BwxLOsPPuLrj6x4AYkQ.gi3S56/l1Qgqz/l2',
        email: 'paolo@example.com',
        roles: ['USER', 'ADMIN']
    },
    {
        firstname: 'Matteo',
        lastname: 'Neri',
        username: 'mneri',
        password: '$2a$10$XUFw0JcXbObu5K9T/BwxLOsPPuLrj6x4AYkQ.gi3S56/l1Qgqz/l2',
        email: 'neri@example.com',
        roles: ['USER']
    },
    {
        firstname: 'Simone',
        lastname: 'Mori',
        username: 'smori',
        password: '$2a$10$XUFw0JcXbObu5K9T/BwxLOsPPuLrj6x4AYkQ.gi3S56/l1Qgqz/l2',
        email: 'mori@example.com',
        roles: ['USER']
    }
]);

/* ================= PROJECTS ================ */
db.createCollection('projects');

db.projects.insertMany([
    {
        _id: ObjectId('6817b0735e351d06c10eb2ae'),
        name: 'my project 3',
        owner: 'admin',
        diagrams: [
            /* ---------- Diagram 1 ---------- */
            {
                id: '450b874d-ad1c-483c-aa76-029c947b9bd9',
                name: 'prova 1',
                data: {
                    nodes: [
                        {
                            _id: '93af563c8079b3dc',
                            label: 'Node A1',
                            shape: 'circle',
                            payload: { code: '{}', language: 'json' },
                            weight: 0
                        },
                        {
                            _id: 'ab7cb3c8a79ba57b',
                            label: 'Node A2',
                            shape: 'circle',
                            payload: { code: '{}', language: 'json' },
                            weight: 0
                        },
                        {
                            _id: '0ccb5966c295015b',
                            label: 'Node A',
                            shape: 'circle',
                            payload: { code: '', language: 'plaintext' },
                            weight: 0
                        }
                    ],
                    connections: [
                        {
                            _id: '08333a012362b30f',
                            source: '93af563c8079b3dc',
                            target: 'ab7cb3c8a79ba57b',
                            isLoop: false,
                            weight: 0,
                            label: 'Node A - Node A',
                            payload: { code: '{}', language: 'json' }
                        },
                        {
                            _id: '719f7098420559ee',
                            source: 'ab7cb3c8a79ba57b',
                            target: '93af563c8079b3dc',
                            isLoop: false,
                            weight: 0,
                            label: 'Node A2 - Node A1',
                            payload: { code: '{}', language: 'json' }
                        },
                        {
                            _id: '6d884d51569cfe5b',
                            source: 'ab7cb3c8a79ba57b',
                            target: '0ccb5966c295015b',
                            isLoop: false,
                            weight: 0,
                            label: 'Node A2 - Node A',
                            payload: { code: '{}', language: 'json' }
                        }
                    ]
                }
            },
            /* ---------- Diagram 2 ---------- */
            {
                id: '61851a12-a958-458e-ac48-21d977826956',
                name: 'prova',
                data: {
                    nodes: [
                        {
                            _id: '93af563c8079b3dc',
                            label: 'Node A',
                            shape: 'circle',
                            payload: { code: '{}', language: 'json' },
                            weight: 1
                        },
                        {
                            _id: 'ab7cb3c8a79ba57b',
                            label: 'Node B',
                            shape: 'circle',
                            payload: { code: 'console.log("3")', language: 'javascript' },
                            weight: 3
                        }
                    ],
                    connections: [
                        {
                            _id: '08333a012362b30f',
                            source: '93af563c8079b3dc',
                            target: 'ab7cb3c8a79ba57b',
                            isLoop: false,
                            weight: 2,
                            label: 'Node A - Node B',
                            payload: { code: '{}', language: 'json' }
                        }
                    ]
                }
            }
        ],
        _class: 'it.univaq.microsynth.domain.Project'
    }
]);
