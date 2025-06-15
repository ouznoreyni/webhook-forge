// Switch to the hookforge database
db = db.getSiblingDB(process.env.MONGO_INITDB_DATABASE || 'webhook-db');

print('🚀 Initializing Noreyni webhook  Database...');

// Create users collection
db.createCollection('users');

print('📋 Creating database indexes...');

// Create indexes for better performance and data integrity
db.users.createIndex({ "email": 1 }, {
    unique: true,
    name: "idx_users_email_unique",
    background: true
});

db.users.createIndex({ "role": 1 }, {
    name: "idx_users_role",
    background: true
});

db.users.createIndex({ "active": 1 }, {
    name: "idx_users_active",
    background: true
});

db.users.createIndex({ "createdAt": 1 }, {
    name: "idx_users_created_at",
    background: true
});

db.users.createIndex({ "updatedAt": 1 }, {
    name: "idx_users_updated_at",
    background: true
});

// Compound index for common queries
db.users.createIndex({ "role": 1, "active": 1 }, {
    name: "idx_users_role_active",
    background: true
});

// Index on first_name and last_name for search
db.users.createIndex({ "first_name": 1, "last_name": 1 }, {
    name: "idx_users_name",
    background: true
});

print('👤 Creating default admin user...');

// Create admin user matching the User entity structure
// Password: "admin123" (hashed with bcrypt)
var adminUser = {
    first_name: "Admin",           // @BsonProperty("first_name")
    last_name: "User",             // @BsonProperty("last_name")
    email: "admin@noreyni.sn",
    password: "$2a$10$rZ8R1wjBhDeX3nUiVCOm7OQQOlPvEk7nEoKjxWJYzQ8zOkXZRqP2e", // admin123
    role: "ADMIN",                 // UserRole.ADMIN
    active: true,
    createdBy: "system",           // BaseEntity audit field
    createdAt: new Date(),         // BaseEntity audit field
    updatedBy: "system",           // BaseEntity audit field
    updatedAt: new Date()          // BaseEntity audit field
};

try {
    var adminResult = db.users.insertOne(adminUser);
    print('✅ Admin user created successfully with ID:', adminResult.insertedId);
} catch (error) {
    if (error.code === 11000) {
        print('ℹ️  Admin user already exists');
    } else {
        print('❌ Error creating admin user:', error.message);
    }
}

print('👥 Creating sample member user...');

// Create sample member user matching the User entity structure
var memberUser = {
    first_name: "Marie",           // @BsonProperty("first_name")
    last_name: "Dubois",           // @BsonProperty("last_name")
    email: "marie.dubois@noreyni.sn",
    password: "$2a$10$rZ8R1wjBhDeX3nUiVCOm7OQQOlPvEk7nEoKjxWJYzQ8zOkXZRqP2e", // admin123
    role: "MEMBER",                // UserRole.MEMBER
    active: true,
    createdBy: "system",           // BaseEntity audit field
    createdAt: new Date(),         // BaseEntity audit field
    updatedBy: "system",           // BaseEntity audit field
    updatedAt: new Date()          // BaseEntity audit field
};

try {
    var memberResult = db.users.insertOne(memberUser);
    print('✅ Sample member user created successfully with ID:', memberResult.insertedId);
} catch (error) {
    if (error.code === 11000) {
        print('ℹ️  Sample member user already exists');
    } else {
        print('❌ Error creating member user:', error.message);
    }
}

print('👥 Creating additional sample users...');

// Create additional sample users for testing
var sampleUsers = [
    {
        first_name: "Jean",
        last_name: "Martin",
        email: "jean.martin@noreyni.sn",
        password: "$2a$10$rZ8R1wjBhDeX3nUiVCOm7OQQOlPvEk7nEoKjxWJYzQ8zOkXZRqP2e", // admin123
        role: "MEMBER",
        active: true,
        createdBy: "system",
        createdAt: new Date(),
        updatedBy: "system",
        updatedAt: new Date()
    },
    {
        first_name: "Sophie",
        last_name: "Diallo",
        email: "sophie.diallo@noreyni.sn",
        password: "$2a$10$rZ8R1wjBhDeX3nUiVCOm7OQQOlPvEk7nEoKjxWJYzQ8zOkXZRqP2e", // admin123
        role: "ADMIN",
        active: true,
        createdBy: "system",
        createdAt: new Date(),
        updatedBy: "system",
        updatedAt: new Date()
    },
    {
        first_name: "Amadou",
        last_name: "Ba",
        email: "amadou.ba@noreyni.sn",
        password: "$2a$10$rZ8R1wjBhDeX3nUiVCOm7OQQOlPvEk7nEoKjxWJYzQ8zOkXZRqP2e", // admin123
        role: "MEMBER",
        active: false, // Inactive user for testing
        createdBy: "system",
        createdAt: new Date(),
        updatedBy: "system",
        updatedAt: new Date()
    }
];

// Insert sample users
sampleUsers.forEach(function(user, index) {
    try {
        var result = db.users.insertOne(user);
        print('✅ Sample user ' + (index + 1) + ' created:', user.first_name + ' ' + user.last_name);
    } catch (error) {
        if (error.code === 11000) {
            print('ℹ️  Sample user already exists:', user.email);
        } else {
            print('❌ Error creating sample user:', error.message);
        }
    }
});

print('📊 Database statistics:');
var totalUsers = db.users.countDocuments({});
var activeUsers = db.users.countDocuments({ active: true });
var adminUsers = db.users.countDocuments({ role: "ADMIN" });
var memberUsers = db.users.countDocuments({ role: "MEMBER" });

print('Total users:', totalUsers);
print('Active users:', activeUsers);
print('Inactive users:', totalUsers - activeUsers);
print('Admin users:', adminUsers);
print('Member users:', memberUsers);

print('📋 Sample user data:');
db.users.find({}).limit(3).forEach(function(user) {
    print('- ' + user.first_name + ' ' + user.last_name + ' (' + user.email + ') - Role: ' + user.role + ', Active: ' + user.active);
});

print('🎉 Database initialization completed successfully!');
print('');
print('👤 Default credentials (all passwords: admin123):');
print('   🔐 Admin: admin@noreyni.sn');
print('   👤 Member: marie.dubois@noreyni.sn');
print('   👤 Admin: sophie.diallo@noreyni.sn');
print('   👤 Member: jean.martin@noreyni.sn');
print('   👤 Member (inactive): amadou.ba@noreyni.sn');
print('');
print('🌐 Access Mongo Express at: http://localhost:8081');
print('   Username: admin');
print('   Password: admin123');
print('');
print('📋 Field mapping (Java entity -> MongoDB document):');
print('   firstName -> first_name');
print('   lastName -> last_name');
print('   email -> email');
print('   password -> password');
print('   role -> role (ADMIN | MEMBER)');
print('   active -> active (boolean)');
print('   createdBy -> createdBy');
print('   createdAt -> createdAt');
print('   updatedBy -> updatedBy');
print('   updatedAt -> updatedAt');
