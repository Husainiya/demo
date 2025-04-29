const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const PDFDocument = require('pdfkit');
const UserModel = require('./models/Users');
const { check, validationResult } = require('express-validator');

const app = express();
app.use(cors());
app.use(express.json());

mongoose.connect("mongodb://localhost:27017/SupplierManagement", {
    useNewUrlParser: true,
    useUnifiedTopology: true
});

// Get all users with optional sorting
app.get('/', (req, res) => {
    const sortField = req.query.sortField || 'name'; // Default sort by name
    const sortOrder = req.query.sortOrder === 'desc' ? -1 : 1; // Default ascending order

    UserModel.find({})
        .sort({ [sortField]: sortOrder })
        .then(users => res.json(users))
        .catch(err => res.status(500).json(err));
});

// Get a single user by ID
app.get('/getUser/:id', (req, res) => {
    const id = req.params.id;
    UserModel.findById(id)
        .then(user => {
            if (user) {
                res.json(user);
            } else {
                res.status(404).json({ message: 'User not found' });
            }
        })
        .catch(err => res.status(500).json(err));
});

// Update a user by ID
app.put('/UpdateUser/:id', [
    check('contact_number').isLength({ min: 10, max: 10 }).withMessage('Contact number should be in 10 digits'),
], (req, res) => {
    const id = req.params.id;
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() });
    }

    UserModel.findByIdAndUpdate(
        id,
        {
            name: req.body.name,
            company_name: req.body.company_name,
            product_name: req.body.product_name,
            contact_number: req.body.contact_number,
            email: req.body.email
        },
        { new: true } // Return the updated document
    )
    .then(user => {
        if (user) {
            res.json(user);
        } else {
            res.status(404).json({ message: 'User not found' });
        }
    })
    .catch(err => res.status(500).json(err));
});

// Delete a user by ID
app.delete('/deleteUser/:id', (req, res) => {
    const id = req.params.id;
    UserModel.findByIdAndDelete(id)
        .then(result => {
            if (result) {
                res.json(result);
            } else {
                res.status(404).json({ message: 'User not found' });
            }
        })
        .catch(err => res.status(500).json(err));
});

// Create a new user
app.post("/CreateUser", [
    check('contact_number').isLength({ min: 10, max: 10 }).withMessage('Contact number should be in 10 digits'),
], (req, res) => {
    const { name, company_name, product_name, contact_number, email } = req.body;
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() });
    }

    if (!name || !company_name || !product_name || !contact_number || !email) {
        return res.status(400).json({ message: 'All fields are required' });
    }

    UserModel.create({ name, company_name, product_name, contact_number, email })
        .then(user => res.status(201).json(user))
        .catch(err => res.status(400).json(err));
});

// Search users based on query
app.get('/search', (req, res) => {
    const { query } = req.query;
    const searchCriteria = {
        $or: [
            { name: { $regex: query, $options: 'i' } }, // Case-insensitive search for name
            { company_name: { $regex: query, $options: 'i' } }, // Case-insensitive search for company name
            { product_name: { $regex: query, $options: 'i' } }, // Case-insensitive search for product name
            { email: { $regex: query, $options: 'i' } } // Case-insensitive search for email
        ]
    };

    UserModel.find(searchCriteria)
        .then(users => res.json(users))
        .catch(err => res.status(500).json(err));
});

// Generate PDF report for selected users
app.post('/generateReport', async (req, res) => {
    try {
        const { userIds } = req.body;
        if (!userIds || userIds.length === 0) {
            return res.status(400).json({ message: 'No users selected' });
        }

        const users = await UserModel.find({ _id: { $in: userIds } }).exec();
        const doc = new PDFDocument();
        const fileName = 'Supplier_report.pdf';

        res.setHeader('Content-disposition', 'attachment; filename=' + fileName);
        res.setHeader('Content-type', 'application/pdf');

        doc.pipe(res);

        doc.fontSize(16).text('Supplier Management Report', { align: 'center' });
        doc.moveDown();
        doc.fontSize(12);

        // Add date and time to the report
        const now = new Date();
        doc.text(`Date: ${now.toLocaleDateString()}`);
        doc.text(`Time: ${now.toLocaleTimeString()}`);
        doc.moveDown();

        users.forEach(user => {
            doc.text(`Name: ${user.name}`);
            doc.text(`Company: ${user.company_name}`);
            doc.text(`Product: ${user.product_name}`);
            doc.text(`Contact: ${user.contact_number}`);
            doc.text(`Email: ${user.email}`);
            doc.moveDown();
        });

        doc.end();
    } catch (error) {
        res.status(500).json({ message: 'Error generating report', error });
    }
});

app.listen(3001, () => {
    console.log("Server is running on port 3001");
});
