# Movie Recomender System
## Description
This project aims to build a movie recommender system with cleaned [Netflix Prize data](https://www.kaggle.com/netflix-inc/netflix-prize-data#probe.txt).
The data is cleaned to the format looks like "userId,movieId,rating".

![屏幕快照 2019-03-19 06.33.16](https://ws3.sinaimg.cn/large/006tKfTcgy1g17phaqghej30q00j2dhw.jpg)



## Guide
### step 1. choose an algorithm - itemCF  
We use itemCF because the number of users weighs more than movies. 
In the meanwhile, movies will not change frequently which helps lower computation.
Last but not least, using user's historical data will be more convincing.



### step 2. describe the relationship between movies - co-occurrence matrix  
We use rating history to define relationship between movies. 
If a user has rated two movies, we consider that these two movies are related.
Then we build a co-occurrence matrix to represent the relationship between different movies, with the format looks like "movieA:movieB relationship".

![屏幕快照 2019-03-19 06.34.56](https://ws4.sinaimg.cn/large/006tKfTcgy1g17pjt365hj30mw0fyq4i.jpg)



Finally, we normalize the co-occurrence matrix to make the result more accurate and transpose the matrix for computing with map reduce to the format looks like "movieB movieA=realtionship".

![屏幕快照 2019-03-19 06.35.33](https://ws1.sinaimg.cn/large/006tKfTcgy1g17plgjj80j30n80fwwha.jpg)



### step 3. build a rating matrix group by user

With the format "userId movieA=rating,movieB=rating,movieC=rating,..."

![屏幕快照 2019-03-19 06.57.44](https://ws4.sinaimg.cn/large/006tKfTcgy1g17q6qt8ehj30s4048dge.jpg)



### step 4. multiply co-occurrence matrix and rating matrix

With the format "userId:movieId multiplyUnitResult"

![屏幕快照 2019-03-19 06.40.51](https://ws4.sinaimg.cn/large/006tKfTcgy1g17ppvoyx2j30m20hqmzj.jpg)



### step 5. sum up and compare  
Then we sum up the result of multiplication grouped by user and movie and get a predicted rating to each movie by each user with the format looks like "userId:movieId predicted_rating"


![屏幕快照 2019-03-19 06.41.19](https://ws2.sinaimg.cn/large/006tKfTcgy1g17ptiu88pj30io0hmtah.jpg)



We compare the predicted rating to the historical rating and find a problem. Let's take user_1's rating for example. We can find that the difference between movie_10001 and movie_10002 rated by user_1 varies from the predicted data to the historical data. Why and how to deal with it?

![屏幕快照 2019-03-19 06.41.33](https://ws1.sinaimg.cn/large/006tKfTcgy1g17ptw9t2pj30jo0hkjve.jpg)



To be continued...



## Reference:
[使用Java API方式的MapReduce练习](https://www.cnblogs.com/frx9527/p/hadoopMR.html)

[用Hadoop构建电影推荐系统](https://www.cnblogs.com/xuxian/p/4122898.html)

[Mapreduce(MR)读取配置文件的三种方式，遍历HDFS目录文件](https://blog.csdn.net/iboyman/article/details/79539549)
