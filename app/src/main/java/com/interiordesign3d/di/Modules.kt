package com.interiordesign3d.di

import com.interiordesign3d.ui.screen.catalogue.CatalogueViewModel
import com.interiordesign3d.ui.screen.catalogue.item.CatalogueItemViewModel
import com.interiordesign3d.ui.screen.designer.DesignerViewModel
import com.interiordesign3d.ui.screen.home.HomeViewModel
import com.interiordesign3d.ui.screen.project.ProjectViewModel
import com.interiordesign3d.ui.screen.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::ProjectViewModel)
    viewModelOf(::CatalogueViewModel)
    viewModelOf(::CatalogueItemViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::DesignerViewModel)
}
