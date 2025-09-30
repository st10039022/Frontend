import {setGlobalOptions} from "firebase-functions";
import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";
import * as sgMail from "@sendgrid/mail";

// Initialize Firebase Admin
admin.initializeApp();

// Set global options
setGlobalOptions({maxInstances: 10});

// Use your SendGrid API key (we're using test123)
const SENDGRID_API_KEY = process.env.SENDGRID_KEY || 'test123';
sgMail.setApiKey(SENDGRID_API_KEY);

// Trigger when volunteer application is updated
export const sendVolunteerStatusEmail = onDocumentUpdated(
  "volunteer_applications/{docId}",
  async (event) => {
    const before = event.data?.before?.data();
    const after = event.data?.after?.data();
    if (!after) return;

    // Only continue if status changed
    if (before?.status === after.status) return;

    const email = after.email;
    const name = after.name;
    const status = after.status;

    if (!email || !name) return;

    let subject = "";
    let text = "";

    if (status === "approved") {
      subject = "Volunteer Application Approved";
      text = `Hi ${name},\n\nYour volunteer application has been approved! Welcome to our team.`;
    } else if (status === "rejected") {
      subject = "Volunteer Application Update";
      text = `Hi ${name},\n\nWe’re sorry, but your volunteer application was not approved at this time.`;
    } else {
      return;
    }

    const msg = {
      to: email,
      from: "noreply@newlifebabyhome.com", // must be verified in SendGrid
      subject: subject,
      text: text
    };

    try {
      await sgMail.send(msg);
      console.log(`Email sent to ${email}`);
    } catch (err) {
      console.error("Error sending email:", err);
    }
  }
);
