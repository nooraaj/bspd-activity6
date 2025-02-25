package com.bsp.fsccis.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class WeekDaysUtil {
	
	static long countBankingDays(Date start, Date end, List<Date> holidays){
		long bankingDays = countWorkingDays(start, end);
		long holidaysOnWeekdays = 0;
		
		Calendar cal = new GregorianCalendar();
		for(Date holiday : holidays){
			cal.setTime(holiday);
			if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
					cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				holidaysOnWeekdays++;
			}
		}
		return bankingDays - holidaysOnWeekdays;
	}
	
	/**
	 * source: http://stackoverflow.com/questions/4600034/calculate-number-of-weekdays-between-two-dates-in-java#answer-4600534
	 * @param start
	 * @param end
	 * @return
	 */
	static long countWorkingDays(Date start, Date end) {
		// Ignore argument check

		Calendar c1 = Calendar.getInstance();
		c1.setTime(start);
		int w1 = c1.get(Calendar.DAY_OF_WEEK);
		c1.add(Calendar.DAY_OF_WEEK, -w1);

		Calendar c2 = Calendar.getInstance();
		c2.setTime(end);
		int w2 = c2.get(Calendar.DAY_OF_WEEK);
		c2.add(Calendar.DAY_OF_WEEK, -w2);

		// end Saturday to start Saturday
		long days = (c2.getTimeInMillis() - c1.getTimeInMillis())
				/ (1000 * 60 * 60 * 24);
		long daysWithoutWeekendDays = days - (days * 2 / 7);

		// Adjust days to add on (w2) and days to subtract (w1) so that Saturday
		// and Sunday are not included
		if (w1 == Calendar.SUNDAY && w2 != Calendar.SATURDAY) {
			w1 = Calendar.MONDAY;
		} else if (w1 == Calendar.SATURDAY && w2 != Calendar.SUNDAY) {
			w1 = Calendar.FRIDAY;
		}

		if (w2 == Calendar.SUNDAY) {
			w2 = Calendar.MONDAY;
		} else if (w2 == Calendar.SATURDAY) {
			w2 = Calendar.FRIDAY;
		}

		return daysWithoutWeekendDays - w1 + w2;
	}
}
