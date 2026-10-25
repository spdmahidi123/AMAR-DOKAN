package com.example.ui.localization

import java.text.NumberFormat
import java.util.Locale

enum class AppLanguage {
    BANGLA,
    ENGLISH
}

object AppStrings {
    // Number conversion to Bengali digits
    fun formatBengaliDigits(input: String): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatPrice(amount: Double, language: AppLanguage): String {
        val intVal = amount.toInt()
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(intVal)
        return if (language == AppLanguage.BANGLA) {
            "৳${formatBengaliDigits(formatted)}"
        } else {
            "৳$formatted"
        }
    }

    fun formatQuantity(qty: Int, language: AppLanguage): String {
        return if (language == AppLanguage.BANGLA) {
            "${formatBengaliDigits(qty.toString())}টি"
        } else {
            "$qty pcs"
        }
    }

    fun searchPlaceholder(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "পণ্য খুঁজুন (যেমন: জামদানি, মধু, পাঞ্জাবি)..."
        AppLanguage.ENGLISH -> "Search products (e.g. Jamdani, Honey, Panjabi)..."
    }

    fun heroTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "দেশসেরা খাঁটি পণ্যের সমাহার"
        AppLanguage.ENGLISH -> "Authentic Bangladeshi Heritage"
    }

    fun heroSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "সরাসরি দেশীয় কারিগর ও বাগান থেকে সংগৃহীত। দ্রুত ও বিশ্বস্ত ক্যাশ অন ডেলিভারি।"
        AppLanguage.ENGLISH -> "Direct from local weavers and farms. Fast, reliable Cash on Delivery nationwide."
    }

    fun featuredTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "জনপ্রিয় কালেকশন"
        AppLanguage.ENGLISH -> "Featured Collection"
    }

    fun allProductsTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "সকল পণ্য"
        AppLanguage.ENGLISH -> "All Products"
    }

    fun addToCart(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "কার্টে যোগ করুন"
        AppLanguage.ENGLISH -> "Add to Cart"
    }

    fun buyNow(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "এখনই কিনুন"
        AppLanguage.ENGLISH -> "Buy Now"
    }

    fun cart(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "শপিং কার্ট"
        AppLanguage.ENGLISH -> "Shopping Cart"
    }

    fun checkout(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "অর্ডার সম্পন্ন করুন"
        AppLanguage.ENGLISH -> "Proceed to Checkout"
    }

    fun subtotal(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "পণ্যের মোট মূল্য"
        AppLanguage.ENGLISH -> "Subtotal"
    }

    fun deliveryCharge(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "ডেলিভারি চার্জ"
        AppLanguage.ENGLISH -> "Delivery Fee"
    }

    fun totalAmount(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "সর্বমোট প্রদেয়"
        AppLanguage.ENGLISH -> "Total Payable"
    }

    fun insideDhaka(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "ঢাকার ভেতরে"
        AppLanguage.ENGLISH -> "Inside Dhaka"
    }

    fun outsideDhaka(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "ঢাকার বাইরে"
        AppLanguage.ENGLISH -> "Outside Dhaka"
    }

    fun cashOnDelivery(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "ক্যাশ অন ডেলিভারি (পণ্য হাতে পেয়ে মূল্য পরিশোধ)"
        AppLanguage.ENGLISH -> "Cash on Delivery (Pay when received)"
    }

    fun adminPortal(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "অ্যাডমিন প্যানেল"
        AppLanguage.ENGLISH -> "Admin Portal"
    }

    fun backToStore(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "স্টোরে ফিরে যান"
        AppLanguage.ENGLISH -> "Back to Store"
    }
}
