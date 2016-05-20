library(orddom) # for cliff's delata and CI 
library(pastecs) #for descriptive stats
options(scipen=100)
options(digits=2)

# returns an array of p values as returned by wilcox.test
runWilcox <- function(system, metric, path) {
  
  #Assume the CSV file is called System_Metric.csv
  pattern = paste("R__", system, "_", metric, ".csv", sep="")
  
  Mj <- NULL
  
  pathname <- file.path(path, pattern) 
  MjTable <- read.csv(pathname, sep=',', header=T)
  
  #summary stats
  summary(MjTable)
  
  testResult <- wilcox.test(MjTable$ClusterSize, alternative="greater")
  Mj <- c(Mj, testResult$p.value)
  
  #Cliff's delta calculations
  #orddom(MjTable$Control,MjTable$ClusterSize,paired=FALSE,outputfile="results.txt")
  return(Mj)
}

runsave <- function(metric, system) {
  # Find CSV path
  wd <- getwd() 
  csvPath <- file.path(wd,"csv") 
  
  # Run the test
  data <- runWilcox(system, metric, csvPath)
  
  # Save the result to file
  # Use the same filename, remove the extension, and append ".txt"
  fname = paste(system,"_",metric,".txt", sep="")
  outputFile <- file.path(csvPath, fname)
  
  #TODO: add some description to the file contents
  write.table(data, file=outputFile, col.names = TRUE, row.names= FALSE)
  #TODO: Return something better
  return(data)
}


metricNames <-c('1FnE')
sapply(metricNames, runsave, "MD")

#Show histogram
csvfile <- "./csv/R__MD_PTEP.csv"
X1FnE <- read.csv(file=csvfile, sep=',', header=T)
hist(X1FnE$ClusterSize)
#barplot(X1FnE$ClusterSize)

