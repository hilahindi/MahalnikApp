// Load environment variables from .env
require("dotenv").config();


// Import Firebase Functions v2 Firestore trigger
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const sgMail = require("@sendgrid/mail");
const fs = require("fs");
const path = require("path");

admin.initializeApp();

// Set SendGrid API key from environment variables
sgMail.setApiKey(process.env.SENDGRID_KEY);

// Load the logo file from the project directory
const logoPath = path.join(__dirname, "mahal_logo.png");
const logoBase64 = fs.readFileSync(logoPath).toString("base64");


exports.sendRequestEmails = onDocumentCreated("requests/{requestId}", async (event) => {
    const data = event.data.data();

    const subject = data.subject || "פנייה חדשה";
    const message = data.message || "";
    const fileUrl = data.fileUrl || "ללא קובץ מצורף";
    const userId = data.userId;

    let fullName = "";
    let userEmail = "";

    // Get user's email and full name from Firestore and Auth
    if (userId) {
        try {
            // Get email from Firebase Auth
            const userRecord = await admin.auth().getUser(userId);
            userEmail = userRecord.email;

            // Get full name from Firestore users collection
            const userDoc = await admin.firestore().collection("users").doc(userId).get();
            if (userDoc.exists) {
                fullName = userDoc.data().fullName || "";
            }
        } catch (err) {
            console.error("Error getting user info:", err);
        }
    }

    // Send email to the organization
    try {
        await sgMail.send({
            to: process.env.SENDGRID_TEAM,
            from: process.env.SENDGRID_FROM,
            subject: `פנייה חדשה: ${subject}`,
            html: `
                <div style="font-family: Arial, sans-serif; direction: rtl;">
                    <img src="cid:mahalLogo" style="width:150px; margin-bottom:20px;">
                    <h2>פנייה חדשה מהאפליקציה</h2>
                    <p><strong>נושא:</strong> ${subject}</p>
                    <p><strong>הודעה:</strong> ${message}</p>
                    <p><strong>קובץ:</strong> ${fileUrl}</p>
                </div>
            `,
            attachments: [
                {
                    content: logoBase64,
                    filename: "mahal_logo.png",
                    type: "image/png",
                    disposition: "inline",
                    content_id: "mahalLogo"
                }
            ]
        });
        console.log("Email sent to organization");
    } catch (err) {
        console.error("Error sending email to organization:", err);
    }

    // Send confirmation email to the user
    if (userEmail) {
        try {
            await sgMail.send({
                to: userEmail,
                from: process.env.SENDGRID_FROM,
                subject: "קיבלנו את הפנייה שלך",
                html: `
                    <div style="font-family: Arial, sans-serif; direction: rtl;">
                        <img src="cid:mahalLogo" style="width:150px; margin-bottom:20px;">
                        <p>שלום ${fullName},</p>
                        <p>קיבלנו את הפנייה שלך בנושא "${subject}" והיא תטופל תוך מספר ימים.</p>
                        <p>בברכה,<br>צוות מח"ל</p>
                    </div>
                `,
                attachments: [
                {
                    content: logoBase64,
                    filename: "mahal_logo.png",
                    type: "image/png",
                    disposition: "inline",
                    content_id: "mahalLogo"
                }
            ]
            });
            console.log("Email sent to user");
        } catch (err) {
            console.error("Error sending email to user:", err);
        }
    }
});
