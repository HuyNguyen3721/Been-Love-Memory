package com.eco.beenlovememory.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class BaseViewPagerAdapter(fragmentManager: FragmentManager, behavior: Int) :
    FragmentPagerAdapter(fragmentManager, behavior) {
    val pageModels: MutableList<PageModel> = mutableListOf()
    override fun getCount(): Int {
        return pageModels.size
    }

    override fun getItem(position: Int): Fragment {
        return pageModels[position].fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return pageModels[position].title
    }

    fun addFragment(fragment: Fragment, title: String?) {
        val model = PageModel(fragment, title)
        pageModels.add(model)
    }

//    fun getTabViewMusic(pos: Int, con: Context): View {
//        val binding = CustomTabViewBinding.inflate(LayoutInflater.from(con), null, false)
//        binding.txtTitle.text = pageModels[pos].title
//        return binding.root
//    }
//    fun getTabViewTemp(pos: Int, con: Context): View {
//        val binding = CustomTabViewTempBinding.inflate(LayoutInflater.from(con), null, false)
//        binding.txtTitle.text = pageModels[pos].title
//        return binding.root
//    }

    inner class PageModel {
        var fragment: Fragment
        var title: String? = null
        var resIconId = 0

        constructor(fragment: Fragment, title: String?, resIconId: Int) {
            this.fragment = fragment
            this.title = title
            this.resIconId = resIconId
        }

        constructor(fragment: Fragment, title: String?) {
            this.fragment = fragment
            this.title = title
        }

    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

}