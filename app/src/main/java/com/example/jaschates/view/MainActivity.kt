package com.example.jaschates.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.jaschates.R
import com.example.jaschates.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

private lateinit var homeFragment: HomeFragment
private lateinit var chatFragment: ChatFragment
private lateinit var profileFragment: ProfileFragment

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.bottomNav.setOnNavigationItemSelectedListener(BottomNavItemSelectedListener)
        homeFragment = HomeFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragments_frame, homeFragment).commit()

    }
    private val BottomNavItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener{
        when(it.itemId){
            R.id.menu_home -> {
                homeFragment = HomeFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, homeFragment).commit()
            }
            R.id.menu_chat -> {
                chatFragment = ChatFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, chatFragment).commit()
            }
            R.id.menu_profile -> {
                profileFragment = ProfileFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, profileFragment).commit()
            }
        }
        true
    }


}