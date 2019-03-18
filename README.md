# MovieRecomenderSystem
## Description
This project aims to build a movie recommender system with cleaned [Netflix Prize data](https://www.kaggle.com/netflix-inc/netflix-prize-data#probe.txt).
The data is cleaned to the format looks like : "userId,movieId,rating".

## Guide
### step 1. choose an algorithm - itemCF  
We use itemCF because the number of users weighs more than movies. 
In the meanwhile, movies will not change frequently which helps lower computation.
Last but not least, using user's historical data will be more convincing.

### step 2. describe the relationship between movies - co-occurrence matrix  
We use rating history to define relationship between movies. 
If a user has rated two movies, we consider that these two movies are related.
Then we build a co-occurrence matrix to represent the relationship between different movies.
Finally, we normalize the co-occurrence matrix to make the result more accurate.

### step 3. build a rating matrix group by user

### step 4. multiply co-occurrence matrix and rating matrix

### step 5. sum up the result of multiplication group by user and movie  
Then we get a predicted rating to each movie by each user.
Then we can recommend top k movies in predicted rating to each user.

## Reference:
[使用Java API方式的MapReduce练习](https://www.cnblogs.com/frx9527/p/hadoopMR.html)

[用Hadoop构建电影推荐系统](https://www.cnblogs.com/xuxian/p/4122898.html)

[Mapreduce(MR)读取配置文件的三种方式，遍历HDFS目录文件](https://blog.csdn.net/iboyman/article/details/79539549)
