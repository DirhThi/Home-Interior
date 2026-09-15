package com.interiordesign3d.ui.screen.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.data.models.DesignRoom
import com.interiordesign3d.data.repository.AppDatabase
import com.interiordesign3d.ui.navigation.Dest
import com.interiordesign3d.ui.screen.home.state.HomeState
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
) : BaseViewModel(app, backStack) {

    private val db = AppDatabase.getInstance(app)

    val screenState: HomeState = object : HomeState() {
        override fun onNewProject() = createProject()
        override fun onExplore() = navigateTo(Dest.ScrCatalogue)
    }

    init {
        viewModelScope.launch {
            db.roomDao().getAllRooms().collect { screenState.projectCount = it.size }
        }
    }

    private fun createProject() {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            db.roomDao().insertRoom(
                DesignRoom(
                    id = id,
                    name = app.getString(R.string.new_room),
                    heightCm = 260f,
                )
            )
            navigateTo(Dest.ScrDesigner(id))
        }
    }
}
