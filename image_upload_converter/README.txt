FORMPAKKA  (version 3.1 — premium interface)
© 2026 Salka Debbarma (salka.debbarma98@gmail.com). All rights reserved.

Prepare exam and job-form uploads: photos, signatures, thumb impressions and
documents, in the exact size and KB each form asks for. Everything runs inside
the browser. No image is ever uploaded to a server.


HOW TO OPEN IT
Open index.html in Google Chrome, Microsoft Edge or Firefox.
Keep the folder together: index.html needs the "assets" and "logo" folders
next to it. (Press Ctrl+F5 once after updating, so the browser loads the new files.)

Top right: language (English, हिन्दी, বাংলা) and the Light / Dark theme.
Every tool has an "Exam / form" button. Choose your exam and the sizes, KB
limits, DPI, file names and rules are filled in for you, or choose
"Custom size" and type the values from your notification.


THE SIX TOOLS
1. Photo & signature resizer
   - Tabs for each document the exam needs (photo, signature, thumb,
     declaration...). A tick shows which ones already have an image.
   - Add an image: upload, drag and drop, paste (Ctrl+V), iPhone HEIC photos,
     or "Use camera" (with an on-screen guide).
   - Crop, rotate & straighten (with a straighten slider and auto-frame).
   - Auto-centre face (AI): frames the face passport-style and straightens a
     tilted head.
   - Remove background (AI): white, blue, grey or any colour.
   - Name & date on photo (turned on automatically for NEET).
   - Photo check: brightness, sharpness, background, face size, centring and
     head tilt, with a score. It's advice, not a guarantee.
   - "Download all documents (ZIP)" when more than one document is ready.
2. Auto-crop from form scan: upload one scanned page (image or PDF). The photo,
   signature and thumb impression are found and sized automatically. Drag a box
   if one is wrong.
3. Documents to PDF under KB: combine images and PDFs, reorder / rotate /
   delete pages, choose 50 KB ... 1 MB, colour / grey / clean scan, A4 or
   original page size, PDF or JPG output.
4. Draw your signature: sign with finger, stylus or mouse (black or blue pen).
5. Passport photo print sheet: 4x6 inch, 5x7 inch or A4 with cut lines, as a
   300 DPI JPG or a PDF. "Use my converted photo" takes the photo from tool 1.
6. Bulk resize (for cyber cafés): convert many images to one exam size and
   download a ZIP, with original or numbered file names.


WHAT NEEDS INTERNET (only the first time)
- AI background remover and face tools: Google MediaPipe model (a few MB).
- Reading PDF files (pdf.js) and iPhone HEIC photos (heic2any).
- The Inter font (without it, the system font is used).
Everything else works offline.


UPDATING EXAM SIZES   ->  assets/presets.js
Open it in Notepad. Each exam is a block like:
  { id: "ibps", group: "banking", name: "IBPS (PO / Clerk / RRB / SO)", site: "https://www.ibps.in",
    docs: [
      { id: "photo", kind: "photo", label: "photo", w: 200, h: 230, kb: [20, 50], dpi: 200, file: "photo.jpg", rules: [...] },
      ...
    ] }
- Size in pixels (w, h), or in cm:  cm: [3.5, 4.5]  or inches:  inch: [4, 6]  (with dpi).
- kb: [minimum, maximum].   file: the download name.
- Copy a block to add a new exam. Change "checked" at the top to the month you
  checked the notifications. Requirements change every year: please re-check
  the official notifications before launch and every exam season.


TRANSLATIONS   ->  assets/i18n.js
All text in English, Hindi and Bengali. Edit the words between the quotes and
keep {words in curly brackets} unchanged. Please have a native speaker review
the Hindi and Bengali before launch.


PUBLISHING IT ONLINE
Upload the whole folder to any static web host with HTTPS, for example
GitHub Pages, Netlify, Cloudflare Pages or Firebase Hosting. When it is online:
- People can install it like an app (the browser offers "Install app").
- It works offline after the first visit (sw.js keeps a copy of the app and,
  after first use, of the AI models).
- After changing any file, raise VERSION at the top of sw.js (for example
  "iuc-v3.0.1") so installed copies update.

Optional: host the AI files yourself (faster, no dependency on the CDN).
Create a folder "ai" next to index.html:
  ai/tasks-vision/vision_bundle.mjs   and   ai/tasks-vision/wasm/...
      (from the npm package @mediapipe/tasks-vision, version 0.10.14)
  ai/models/selfie_multiclass_256x256.tflite
  ai/models/blaze_face_short_range.tflite
      (from https://storage.googleapis.com/mediapipe-models/)
The app looks there first and falls back to the public CDN.


LAUNCH CHECKLIST
1. Re-check every exam size in presets.js against the latest notifications.
2. Put it online with HTTPS (see above) and test on a phone.
3. In index.html, change og:image to the full address, e.g.
   https://your-site.com/logo/logo-icon-512.png (needed for WhatsApp and
   Facebook link previews), and add <link rel="canonical" href="https://your-site.com/">.
4. Read privacy.html and terms.html. They are a starting point, not legal
   advice: have them reviewed, and add anything your hosting or country needs.
5. Add the site to Google Search Console.


FILES
index.html            the app
privacy.html          privacy policy
terms.html            terms of use
manifest.webmanifest  app name and icons (for installing)
sw.js                 offline support
assets/app.css        design (colours, light and dark theme)
assets/app.js         screens and tools
assets/engine.js      image processing (resize, KB, AI, PDF, ZIP)
assets/presets.js     exam sizes
assets/i18n.js        English, Hindi and Bengali text
assets/icons/         app icons
logo/                 logo files (SVG and PNG, light and dark)
index_v2_backup.html  previous version (single page)
index_old_backup.html the original version


PREMIUM INTERFACE UPDATE — 28 SEPTEMBER 2026
- A green-and-ivory interface with matching dark mode.
- Animated example workspace, signature drawing, card entrances and hover states.
- Motion respects the device's Reduce Motion preference.
- Direct navigation between all six tools; keyboard skip link and route focus.
- Responsive layouts and updated English, Hindi and Bengali interface copy.
- New design files: assets/premium.css and assets/premium.js.
- Offline app cache version: iuc-v3.1.1.
- Core conversion code and exam requirements are unchanged.

Open index.html to use the updated version. Keep all files in this folder
together. For a hosted installation, refresh once after updating the files.
An original-file backup is saved beside this folder in
design-backup-2026-09-28.

EXAM QUICK START CARDS
The homepage exam cards are clickable. IBPS, SBI, SSC, UPSC, NEET, JEE and
CUET each open the resizer with the matching existing preset selected.
Custom size opens the editable dimension and file-size controls. Cards have
staggered entrances, hover/focus motion, and a remembered selection marker.
Reduced Motion disables the animations.
