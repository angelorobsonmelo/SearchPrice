package com.example.searchprice.di

import com.example.searchprice.data.repository.PriceRepositoryImpl
import com.example.searchprice.data.source.RemotePriceDataSource
import com.example.searchprice.domain.repository.PriceRepository
import com.example.searchprice.domain.usecase.GetLocationUseCase
import com.example.searchprice.domain.usecase.SearchProductsUseCase
import com.example.searchprice.presentation.viewmodel.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { RemotePriceDataSource() }
    single<PriceRepository> { PriceRepositoryImpl(get()) }
    factory { SearchProductsUseCase(get()) }
    factory { GetLocationUseCase() }
    viewModel { SearchViewModel(get(), get()) }
}
