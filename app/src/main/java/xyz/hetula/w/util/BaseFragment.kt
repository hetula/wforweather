package xyz.hetula.w.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import xyz.hetula.w.MainActivity

abstract class BaseFragment : Fragment() {
    protected lateinit var mainActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity !is MainActivity) {
            throw IllegalStateException("BaseFragment not in MainActivity!")
        }
        mainActivity = activity as MainActivity
    }
}
