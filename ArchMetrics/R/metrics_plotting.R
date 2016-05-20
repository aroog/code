plotMetric<- function(system, metric, col='ClusterSize', plot_fun = "hist", xlab=NULL, ylab=NULL, main=NULL, cex.axis=2.5, cex.names=1.45, cex.lab=2.5, cex.main=2.5){


  switch(plot_fun, 
         hist={
           xlab=col
           ylab="Frequency"
         },
         boxplot={
           ylab=col
           xlab=metric
         },
         barplot={
           xlab=metric
           ylab=col
         },qqnorm={
           xlab = ""
           ylab = ""

         })
  main<- system
  FUN <- match.fun(plot_fun)
  csvPath <- file.path(getwd() ,"csv")
  pattern <- paste("R__",system,"_",metric,".csv", sep="")
  pathname <-file.path(csvPath, pattern)
  csv_data <- read.csv(file=pathname, sep=',', header=T)
  setEPS()
  fileName<- paste(system, "_", metric,"_",col,"_", plot_fun, sep="") 
  addEpsToBeamerFile(metricName=metric, epsFile=fileName, plotFunc=plot_fun, system=system)
  postscript(paste("./eps/",fileName, ".eps", sep=""))
  FUN(csv_data[,col], main=main, xlab=xlab, ylab=ylab, cex.axis=cex.axis,cex.names=cex.names, cex.lab=cex.lab, cex.main=cex.main)
  dev.off()
}



plotAllSystem<- function(metric, table, plot_fun ="barplot", xlab=NULL,ylab=NULL, main=NULL,cex.axis=2.5, cex.names=1.45, cex.lab=2.5, cex.main=2.5){
  displayName<- getDisplayName(metric)
  
  FUN <- match.fun(plot_fun)
  fileName <- paste("across_systems_", metric,"_", plot_fun, sep="")
  addEpsToBeamerFile(metricName=metric, epsFile=fileName, plotFunc=plot_fun)
  setEPS()
  postscript(paste("./eps/",fileName, ".eps", sep=""))
  FUN(getUnlistedMetric(metric, table), main=displayName, xlab=xlab, ylab=ylab, cex.axis=cex.axis,cex.names=cex.names, cex.lab=cex.lab, cex.main=cex.main)
  dev.off()
}

#NOTE: plot_fun is not being used! This works only with boxplots.
plotRelatedMetrics<-function(metrics, table, main="Related Metrics", plot_fun="boxplot", xlab=NULL,ylab=NULL,cex.axis=1.5, cex.main=1.5){

  displayNames<-sapply(metrics, getDisplayName)
  file_title <- paste(metrics, collapse="_")
  fileName<-paste("related_metrics_", file_title,"_", plot_fun, sep="")
  addEpsToBeamerFile(metricName=main, epsFile=fileName, plotFunc=plot_fun)
  setEPS()
  postscript(paste("./eps/",fileName,".eps", sep=""), pointsize=10)
  data<-lapply(metrics, getUnlistedMetric, table)
 
  bpdata<-boxplot(data, names=displayNames, main=main, xlab=xlab,ylab=ylab, cex.axis=cex.axis,cex.main=cex.main)
  if(plot_fun == "boxplot"){
    #TOAND: HIGH. Investigate: this sometimes generates error: "no coordinates were supplied"
    if(!identical(bpdata$out, numeric(0))){
      text(bpdata$group+.1,  bpdata$out, names(bpdata$out), cex=1.5)
    }
    
  }
  dev.off()
  
}