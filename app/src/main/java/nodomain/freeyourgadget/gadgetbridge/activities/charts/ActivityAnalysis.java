/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.activities.charts;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.model.ActivityAmount;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityAmounts;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;

class ActivityAnalysis {
    ActivityAmounts calculateActivityAmounts(List<? extends ActivitySample> samples) {
        ActivityAmount deepSleep = new ActivityAmount(ActivityKind.TYPE_DEEP_SLEEP);
        ActivityAmount lightSleep = new ActivityAmount(ActivityKind.TYPE_LIGHT_SLEEP);
        ActivityAmount notWorn = new ActivityAmount(ActivityKind.TYPE_NOT_WORN);
        ActivityAmount activity = new ActivityAmount(ActivityKind.TYPE_ACTIVITY);

        ActivityAmount previousAmount = null;
        ActivitySample previousSample = null;
        for (ActivitySample sample : samples) {
            ActivityAmount amount;
            switch (sample.getKind()) {
                case ActivityKind.TYPE_DEEP_SLEEP:
                    amount = deepSleep;
                    break;
                case ActivityKind.TYPE_LIGHT_SLEEP:
                    amount = lightSleep;
                    break;
                case ActivityKind.TYPE_NOT_WORN:
                    amount = notWorn;
                    break;
                case ActivityKind.TYPE_ACTIVITY:
                default:
                    amount = activity;
                    break;
            }

            int steps = sample.getSteps();
            if (steps > 0) {
                amount.addSteps(sample.getSteps());
            }

            if (previousSample != null) {
                long timeDifference = sample.getTimestamp() - previousSample.getTimestamp();
                if (previousSample.getRawKind() == sample.getRawKind()) {
                    amount.addSeconds(timeDifference);
                } else {
                    long sharedTimeDifference = (long) (timeDifference / 2.0f);
                    previousAmount.addSeconds(sharedTimeDifference);
                    amount.addSeconds(sharedTimeDifference);
                }
            }

            previousAmount = amount;
            previousSample = sample;
        }

        ActivityAmounts result = new ActivityAmounts();
        if (deepSleep.getTotalSeconds() > 0) {
            result.addAmount(deepSleep);
        }
        if (lightSleep.getTotalSeconds() > 0) {
            result.addAmount(lightSleep);
        }
        if (activity.getTotalSeconds() > 0) {
            result.addAmount(activity);
        }
        result.calculatePercentages();

        return result;
    }

    int calculateTotalSteps(List<? extends ActivitySample> samples) {
        int totalSteps = 0;
        for (ActivitySample sample : samples) {
            int steps = sample.getSteps();
            if (steps > 0) {
                totalSteps += sample.getSteps();
            }
        }
        return totalSteps;
    }
}
