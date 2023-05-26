package com.example.catchycraft

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.catchycraft.databinding.ActivityBuyerBottomNavigationBinding

class BuyerBottomNavigation : AppCompatActivity() {
    private lateinit var binding: ActivityBuyerBottomNavigationBinding
    override fun onBackPressed() {
        return
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBuyerBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repalceFragment(BuyersHome())

        binding.buyerBottomNavigation.setOnItemSelectedListener{
            when(it.itemId){
                R.id.buyersHome->repalceFragment(BuyersHome())
                R.id.buyersNotification->repalceFragment(BuyersNotification())
                R.id.buyersInquries->repalceFragment(BuyersInquire())
                R.id.buyersAccount->repalceFragment(BuyresAccount())
                else->{

                }
            }
            true
        }
    }
    private  fun repalceFragment(fragment: Fragment){
        val fragmentManager=supportFragmentManager
        val fragmentTransaction=fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.buyerFrameLayout,fragment)
        fragmentTransaction.commit()


    }
}