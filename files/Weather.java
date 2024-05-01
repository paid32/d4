// Locate dataset (e.g., sample_weather.txt) for working on weather data which reads the text
// input files and finds average for temperature, dew point and wind speed.

import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Weather {
    public static class WeatherMapper extends Mapper<Object, Text, Text, Text> {
        private Text date = new Text();
        private Text weatherData = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] data = value.toString().split("\\s+");
            String d = data[2];
            date.set(String.format("%s-%s-%s", d.substring(0, 4), d.substring(4,
                    6), d.substring(6, 8)));
            weatherData.set(String.format("%s %s %s", data[3], data[4], data[12]));
            context.write(date, weatherData);
        }
    }

    public static class WeatherReducer extends Reducer<Text, Text, Text, Text> {
        private Text result = new Text();

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            float sumTemperature = 0, sumDewPoint = 0, sumWindSpeed = 0;
            int count = 0;
            for (Text val : values) {
                String[] weatherData = val.toString().split("\\s+");
                sumTemperature += Float.parseFloat(weatherData[0]);
                sumDewPoint += Float.parseFloat(weatherData[1]);
                sumWindSpeed += Float.parseFloat(weatherData[2]);
                count++;
            }
            float avgTemperature = sumTemperature / count;
            float avgDewPoint = sumDewPoint / count;
            float avgWindSpeed = sumWindSpeed / count;
            String resultStr = String.format("%.4f %.4f %.4f", avgTemperature, avgDewPoint, avgWindSpeed);
            result.set(resultStr);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Weather");
        job.setJarByClass(Weather.class);
        job.setMapperClass(WeatherMapper.class);
        job.setCombinerClass(WeatherReducer.class);
        job.setReducerClass(WeatherReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

// INPUT:
// 690190 13910 20060201_0 51.75 33.0 24 1006.3 24 943.9 24 15.0 24 10.7 24 22.0 28.9 0.00I 999.9 000000
// 690190 13910 20060201_1 54.74 33.0 24 1006.3 24 943.9 24 15.0 24 10.7 24 22.0 28.9 0.00I 999.9 000000
// 690190 13910 20060201_2 50.59 33.0 24 1006.3 24 943.9 24 15.0 24 10.7 24 22.0 28.9 0.00I 999.9 000000
// 690190 13910 20060201_3 51.67 33.0 24 1006.3 24 943.9 24 15.0 24 10.7 24 22.0 28.9 0.00I 999.9 000000