# QR Scanner App

A **simple QR scanner app**. It can scan **all types of QR codes**, including UPI payment QR codes.

---

## 📱 Features

1. **Scan any type of QR code.**
2. Shows a **toast message** with the content and **copies it to clipboard**.
3. Shows avaiable app for **UPI payment** if any.

---

## 🚀 How to Use

Use the three dots (⋮) menu in the top-right corner and try scanning with the different available options.  
If **none** of the options work, it's possible:

- All your phone’s **sensors are defective**, or
- The **physical sensors are not exposed** to app layer by your chipset vendor.

Read below for more technical context.

---

## 📸 How Android Camera Works (TL;DR)

Android defines two types of cameras:

1. **Physical Camera**  
   The actual lenses/sensors on your device (e.g., wide, ultra-wide, telephoto).
2. **Logical Camera**  
   A combination of one or more physical cameras used for advanced processing (e.g., portrait mode).

> Google does not enforce standard rules for how camera hardware is exposed via the **Camera HAL** to the **Camera2 API**.  
> So behavior varies across **chipset vendor** (Qualcomm, MediaTek, Exynos, etc.):

- Some vendors expose **only logical** cameras.
- Some vendors expose **only physical**.
- Some vendors expose **both**.

And each camera either **physical** or **logical** have a associated **camera id**. **Chipset vendors may or may not expose underlying physical cameras id for a logical camera id.**
Even if it is exposed **this app only uses camera id to access camera may it be physical or logical.**
  
This means:
- If your **wide sensor is defective** and your vendor does not expose physical cameras for other sensors, you may not be able to use this app effectively.
- If a **logical camera** includes the defective wide sensor, you’ll get a blank or stuck preview for that camera id.
- But if a **logical camera id do not have wide sensor** then it can be used.

To learn more, search for:
- `Camera HAL Android`
- `CameraX API`
- `Camera2 API`
- Or just ask ChatGPT 😊

---

## ✅ Why Use This App?

There are many QR scanner apps on the Play Store, **even stock camera apps** can scan QR codes.  
So why this one?

**Because this app allows you to _switch sensors_!**

- Scan with **front camera**, or **any available back camera**.
- Perfect if **your main camera sensors isn’t working**.
- Most QR/UPI apps **do not** let you change the camera used for scanning.

---

## ⚠️ UPI App Compatibility Warning

Some UPI apps **do not alow payment** if the QR is scanned via a **third-party app**, due to strict security or merchant verification policies.

Although:
- The **scan works.**
- Payment screen appears.
- Beneficiary name is displayed.

But:
- Payment fails **after entering the UPI PIN.**
- Error reasons vary by app and even across different transactions.

> As of **3rd August 2025**, after testing various UPI apps, only **Cred** worked reliably.

This limitation exists because:
> **UPI intent** invoked from third-party apps (if not a registered merchants with NPCI) may not be supported by some UPI apps, even though **NPCI specs** mentions to support utility apps like this.

---

## 🙏 Thanks

Thanks to ChatGPT for assistance in building this app and explaining camera intricacies.

---
