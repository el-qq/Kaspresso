package com.kaspersky.kaspresso.instrumental

import android.app.Instrumentation
import android.app.UiAutomation
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.test.uiautomator.UiDevice
import com.kaspersky.components.kautomator.common.Environment
import com.kaspersky.components.kautomator.common.environment
import com.kaspersky.kaspresso.instrumental.exception.NotSupportedEnvironment
import com.kaspersky.kaspresso.instrumental.exception.NotSupportedInstrumentalTestException

internal class InstrumentalDependencyProviderImpl(
    private val location: InstrumentalUsage,
    private val instrumentation: Instrumentation
) : InstrumentalDependencyProvider {

    override val isAndroidRuntime: Boolean =
        when (val environment = environment) {
            is Environment.AndroidRuntime -> true
            is Environment.Robolectric -> false
            is Environment.Unknown -> {
                throw NotSupportedEnvironment(
                    """

                        The current environment is not supported by Kaspresso.
                        Some additional info about the current environment: $environment.
                        Please let us know by creating an issue if you desire to support a new environment that is differ from Android Runtime or JVM with Robolectric support.

                    """.trimIndent()
                )
            }
        }

    override val uiDevice: UiDevice
        get() =
            if (isAndroidRuntime) UiDevice.getInstance(instrumentation)
            else throw NotSupportedInstrumentalTestException(location, "UiDevice")

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getUiAutomation(flags: Int): UiAutomation {
        if (!isAndroidRuntime) throw NotSupportedInstrumentalTestException(location, "UiAutomation")
        return instrumentation.getUiAutomation(flags)
    }

    override val uiAutomation: UiAutomation
        get() =
            if (isAndroidRuntime) instrumentation.uiAutomation
            else throw NotSupportedInstrumentalTestException(location, "UiAutomation")
}

internal sealed class InstrumentalUsage {
    data class ComponentLocation(val componentName: String) : InstrumentalUsage()
    data class InterceptorLocation(val interceptorName: String) : InstrumentalUsage()
    object TestLocation : InstrumentalUsage()
}
