package org.yardimci;

public class MesafeHesaplayici {
        private static final double YARICAP_DUNYA_KM = 6371.0;

        public static double haversine(double lat1, double lon1, double lat2, double lon2) {
            double latFark = Math.toRadians(lat2 - lat1);
            double lonFark = Math.toRadians(lon2 - lon1);

            double a = Math.pow(Math.sin(latFark / 2), 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.pow(Math.sin(lonFark / 2), 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return YARICAP_DUNYA_KM * c;
        }
}
