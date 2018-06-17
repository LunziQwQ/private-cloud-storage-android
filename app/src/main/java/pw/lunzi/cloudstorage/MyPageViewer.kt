package pw.lunzi.cloudstorage

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

private const val ARG_PAGE_NUMBER = "page_number"

class MyPageViewer:Fragment(){

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layoutList = mutableListOf<View>()
        layoutList.add(layoutInflater.inflate(R.layout.page_common, null))
        layoutList.add(layoutInflater.inflate(R.layout.page_my_space, null))
        layoutList.add(layoutInflater.inflate(R.layout.page_me, null))

        return layoutList[arguments!!.getInt(ARG_PAGE_NUMBER)]
    }

    companion object {
        fun newInstance(pageNumber: Int) : MyPageViewer{
            val args = Bundle()
            args.putInt(ARG_PAGE_NUMBER, pageNumber)
            val page = MyPageViewer()
            page.arguments = args
            return page
        }
    }
}


class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        return MyPageViewer.newInstance(position)
    }
}