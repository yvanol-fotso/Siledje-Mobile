package com.siledje.mobile

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Exposé au projet Xcode (App.swift) via :
 *   ComposeView(controller: MainViewController())
 * Nécessite un Mac + Xcode pour compiler la cible iOS — sur Windows,
 * seule la cible Android est buildable directement.
 */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
