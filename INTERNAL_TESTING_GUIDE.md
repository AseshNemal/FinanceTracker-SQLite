# Internal Testing Setup Guide for Finance Tracker

## Why Use Internal Testing?

Internal testing allows you to:
- Upload and test your app while account verification is pending
- Verify your AAB file works correctly
- Test app functionality before public release
- Bypass some account restrictions temporarily
- Share with up to 100 testers

## Step-by-Step Internal Testing Setup

### Step 1: Access Internal Testing
1. Go to [Google Play Console](https://play.google.com/console)
2. Select your "Finance Tracker" app
3. Navigate to **"Release" → "Testing" → "Internal testing"**
4. Click **"Create new release"**

### Step 2: Upload Your App Bundle
1. Click **"Upload"** button
2. Select your AAB file: `app-release.aab`
3. Wait for upload and processing to complete
4. You should see:
   ```
   Version: 1.0.0 (1)
   Package: com.aseshnemal.financetracker
   Size: ~3.1 MB
   ```

### Step 3: Add Release Notes
Add these release notes:
```
🎉 Internal Testing - Finance Tracker v1.0.0

✨ Features to Test:
• Dashboard with income, expense, and savings overview
• Add/edit/delete transactions across 8 categories
• Monthly budget setting and monitoring
• Interactive charts and analytics (pie, bar, line charts)
• Data export/import functionality
• Offline operation - all data stored locally

🔍 Testing Focus:
• Verify all features work correctly
• Test data persistence after app restart
• Check chart animations and interactions
• Test export/import functionality
• Ensure smooth navigation between screens

📱 Test on different devices and Android versions if possible.
```

### Step 4: Set Up Testers
1. In the **"Testers"** tab, click **"Create email list"**
2. Name it: "Finance Tracker Internal Testers"
3. Add email addresses:
   ```
   your.email@gmail.com
   (Add friends/family if you want additional testers)
   ```
4. Save the email list
5. Select the list for this release

### Step 5: Review and Start Testing
1. Click **"Review release"**
2. Verify all information is correct
3. Click **"Start rollout to Internal testing"**

### Step 6: Install and Test
1. Check your email for testing invitation
2. Click the testing link in the email
3. Install the app on your device
4. Test all features thoroughly
5. Note any issues or improvements needed

## What to Test

### Core Functionality
- [ ] App launches successfully
- [ ] Add new income transaction
- [ ] Add new expense transaction
- [ ] Edit existing transaction
- [ ] Delete transaction
- [ ] Set monthly budget
- [ ] View budget progress
- [ ] Navigate between all screens

### Charts and Analytics
- [ ] Pie chart displays income vs expenses
- [ ] Bar chart shows category breakdown
- [ ] Line chart shows daily trends
- [ ] Charts are interactive (zoom, rotate)
- [ ] Data updates in real-time

### Data Management
- [ ] Export data to file
- [ ] Import data from file
- [ ] Data persists after app restart
- [ ] All transactions saved correctly

### User Interface
- [ ] Navigation works smoothly
- [ ] All buttons respond correctly
- [ ] Text is readable and properly formatted
- [ ] App works in portrait orientation
- [ ] No crashes or freezes

## Promoting to Production

Once internal testing is successful and your account issues are resolved:

1. Go to **"Release" → "Production"**
2. Click **"Create new release"**
3. Copy the same AAB file from internal testing
4. Use the same release notes
5. Complete store listing requirements
6. Submit for review

## Troubleshooting Internal Testing

### "Can't create release"
- Ensure your account has completed basic verification
- Try refreshing the page and logging in again
- Clear browser cache and cookies

### "Upload failed"
- Check internet connection
- Try uploading again after a few minutes
- Ensure AAB file is not corrupted

### "No testers receive invitation"
- Check spam/junk folders
- Ensure email addresses are correct
- Try removing and re-adding testers

### "App won't install"
- Ensure device meets minimum SDK requirements (Android 8.0+)
- Check if device has enough storage space
- Try uninstalling any previous versions

## Benefits of Internal Testing

1. **Risk-Free Testing**: Test without affecting public rating
2. **Quick Feedback**: Get immediate feedback from testers
3. **Bug Detection**: Find issues before public release
4. **Account Bypass**: Work around temporary account restrictions
5. **Confidence Building**: Ensure app works before going live

## Timeline

- **Setup**: 10-15 minutes
- **Upload processing**: 5-10 minutes
- **Tester invitation**: Immediate
- **Installation**: 2-3 minutes
- **Testing period**: 1-7 days (recommended)

Remember: Internal testing is a safe way to verify your app works correctly while your main account verification is being processed!