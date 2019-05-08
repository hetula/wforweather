package xyz.hetula.w.ui

import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class WFragment : Fragment() {
    protected lateinit var mainActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity !is MainActivity) {
            throw IllegalStateException("WFragment not in MainActivity!")
        }
        mainActivity = activity as MainActivity
    }
}
