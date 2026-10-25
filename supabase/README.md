# Amar Dokan (আমার দোকান) - Production Deployment & Supabase Guide

## 1. Free-Tier Architecture Overview
* **Mobile / Android Client**: Kotlin + Jetpack Compose with reactive Room persistence (bundled out-of-the-box for instant offline-first shopping and local store operations).
* **Cloud Database & Auth**: Supabase PostgreSQL + Supabase Auth.
* **Storage**: Supabase Storage for product images.
* **Live Order Notifications**: Supabase Realtime channel on `public.orders`.
* **Hosting**: Cloudflare Pages / Vercel (Free tier) for web storefront, AI Studio / Android Play Store for Android APK.

---

## 2. Supabase Setup Instructions

### Step 1: Create a Free Supabase Project
1. Go to [https://supabase.com](https://supabase.com) and create a free project named `amar-dokan`.
2. Choose the closest region: `Singapore (ap-southeast-1)` or `Mumbai (ap-south-1)` for ultra-low latency from Bangladesh.
3. Note your `Project URL` and `anon` public key from **Project Settings -> API**.

### Step 2: Apply the SQL Schema
1. Open the **SQL Editor** in your Supabase dashboard.
2. Open `supabase/schema.sql` from this project.
3. Paste the entire SQL content into the editor and click **Run**.
4. This will create:
   - `products`, `categories`, `orders`, `order_items`, `inventory_movements`, `business_settings`, `notification_logs`, and `admin_profiles` tables.
   - Row Level Security (RLS) policies protecting customer phone numbers and inventory margins.
   - Realtime publication enabling live order notifications.

### Step 3: Create the First Administrator Account
1. In Supabase Dashboard, go to **Authentication -> Users**.
2. Click **Add User** -> **Create User**, and enter:
   - Email: `admin@amardokan.com.bd` (or your preferred admin email)
   - Password: Your strong admin password.
3. Copy the generated User UUID.
4. Run this query in the SQL Editor to assign admin privileges:
```sql
INSERT INTO public.admin_profiles (id, email, full_name, role)
VALUES ('YOUR_USER_UUID_HERE', 'admin@amardokan.com.bd', 'Chief Administrator', 'super_admin');
```

---

## 3. Order Email Notifications (Free Tier with Resend)
To send email alerts on new customer orders:
1. Create a free account at [https://resend.com](https://resend.com) (free 3,000 emails/month).
2. Create a Supabase Edge Function:
```bash
supabase functions new send-order-email
```
3. Use a Webhook on `public.orders` INSERT to trigger the function with order number, items, total in ৳, and delivery address.

---

## 4. Default Store Credentials in Android App
* **Admin Login PIN**: `1234` (configurable under Admin -> Settings)
* **Default Language**: Bengali (বাংলা) with English toggle
* **Currency**: Bangladeshi Taka (BDT / ৳)
* **Delivery Fees**:
  - Inside Dhaka: ৳60
  - Outside Dhaka: ৳120
