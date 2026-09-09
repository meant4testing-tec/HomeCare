package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DateUtils
import com.example.data.model.TaskUrgency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HomeCare", appName)
  }

  @Test
  fun `test frequency calculation next due date`() {
    val cal = Calendar.getInstance()
    cal.set(2026, Calendar.JANUARY, 1, 10, 0, 0)
    val baseMillis = cal.timeInMillis

    val nextDueMonths = DateUtils.calculateNextDueDate(baseMillis, "MONTHS", 6)
    val checkCal = Calendar.getInstance().apply { timeInMillis = nextDueMonths }
    assertEquals(Calendar.JULY, checkCal.get(Calendar.MONTH))
    assertEquals(1, checkCal.get(Calendar.DAY_OF_MONTH))

    val nextDueDays = DateUtils.calculateNextDueDate(baseMillis, "DAYS", 30)
    val daysCal = Calendar.getInstance().apply { timeInMillis = nextDueDays }
    assertEquals(Calendar.JANUARY, daysCal.get(Calendar.MONTH))
    assertEquals(31, daysCal.get(Calendar.DAY_OF_MONTH))
  }

  @Test
  fun `test urgency calculation`() {
    val today = DateUtils.getStartOfDay()
    val overdueMillis = today - (2L * 24 * 60 * 60 * 1000)
    val dueTodayMillis = today + (1000 * 60 * 60)
    val dueSoonMillis = today + (5L * 24 * 60 * 60 * 1000)
    val upcomingMillis = today + (40L * 24 * 60 * 60 * 1000)

    assertEquals(TaskUrgency.OVERDUE, DateUtils.getUrgency(overdueMillis))
    assertEquals(TaskUrgency.DUE_TODAY, DateUtils.getUrgency(dueTodayMillis))
    assertEquals(TaskUrgency.DUE_SOON, DateUtils.getUrgency(dueSoonMillis))
    assertEquals(TaskUrgency.UPCOMING, DateUtils.getUrgency(upcomingMillis))
  }

  @Test
  fun `test warranty calculation`() {
    val today = DateUtils.getStartOfDay()
    val expiredExpiry = today - (10L * 24 * 60 * 60 * 1000)
    val activeExpiry = today + (120L * 24 * 60 * 60 * 1000)

    val expiredInfo = DateUtils.getWarrantyInfo(expiredExpiry)
    assertTrue(expiredInfo.isExpired)

    val activeInfo = DateUtils.getWarrantyInfo(activeExpiry)
    assertFalse(activeInfo.isExpired)
    assertEquals(120L, activeInfo.daysRemaining)
  }
}
