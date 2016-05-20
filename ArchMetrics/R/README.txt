Copy all CSV files into 'csv' folder.

Use copyCSV.bat
Or
\shared\papers\ArchMetrics\Rcopy-CSV.bat
\shared\papers\ArchMetrics\Rcopy-CSV2.bat

Be sure that system name in "all_metrics.csv" matches the name in the scripts.
"MD_Summary" vs. "MD"

-----------------------------------------------------------------------------------------------------
To re-generate tables in paper:
(use the script that has the same name as the LaTex file that contains the table)

Table: STATISTICAL ANALYSIS FOR SET METRICS:
run: metrics.R
generates LaTex file: metrics.tex


Table: ALL METRICS ACROSS ALL SYSTEMS:
run: test_all_metrics.R

TODO: Rename script: "all_metrics.R"

generates LaTex files:
all_metrics.tex (main one to include in the paper)
Also generates separate files:
cluster_metrics.tex
obj_metrics.tex
edge_metrics.tex
(if we want to break-up the big table into smaller tables)

-----------------------------------------------------------------------------------------------------

To re-generate figures (plots):
run: figures.R

Copy a subset to the paper
shared/papers/ArchMetrics/eps/copyEPS.bat

Re-gen Beamer with all the plots.
shared/ArchMetrics/R/beamer/main.pdf

-----------------------------------------------------------------------------------------------------

To re-generate the All systems table in the paper :
run: all_systems.R

Must also manually combine with other columns.

NOTE: Not currently including the .tex file all_systems.tex

NOTE: The all systems table in the paper is a transposed version: 
instead of showing systems in cols, they are shown in rows

-----------------------------------------------------------------------------------------------------

Then copy files over to paper. Use batch file (UPDATE paths if needed):


papers\ArchMetrics>Rcopy.bat

-----------------------------------------------------------------------------------------------------

Manually update the files:
metrics.tex


Fix headings:
- replace "D-size" with "$\Delta$


all_metrics.tex:
Insert:
\hline \textbf{RQ1}
before WA
\hline \textbf{RQ2}
before HMN
\hline \textbf{RQ3}
before 1PnD
\hline \textbf{RQ4}
before PTEP

metrics.tex:
Insert:

\hline
Before:
  HMO
\hline
Before:
  1PnD


XXX. Manually hack the table:
- Use 0 digits for the first part (xx)
- Use 2 digits for the second part (0.xx)
- Or convert to percentages and use 0 digits everywhere


-----------------------------------------------------------------------------------------------------

Post to online appendix:
- Zip files
- Beamer plots

-----------------------------------------------------------------------------------------------------

XXX. Always make sure that the .csv files contain the same number of columns:
- watch out for R__Si__All_Metrics.csv
- when we add the DF metrics, we cannot add them for one system but not the others
- can enable/disable list of systems from the array in the script
-- one array contains the list of systems
-- another array contains the list of metrics


-----------------------------------------------------------------------------------------------------

DONE. Write down in this file all the steps involved in adding a new metric
- change the arrays
- change metric_name.csv
- etc.
 
- test it out with: PTEP, SO
- make the scripts work
- then add the DF metrics

TOSUM: Dig up notes from when we added PTEP, SO.



**********************************************************************************

Steps involved in adding a new metric

1) Copy the newly generated R__System_Metric.csv into the common csv folder of ArchMetric project
2) Add one row for the metric short name in metric_name.csv along with its corresponding control values. Add one row for Max, Min, StdDev, Median, Mean and Threshold for the new metric
3) Change the metrics.R script and add the metric short name in the array metricNames. 
4) Run the script that generates metrics.tex
5) Change the test_all_metrics.R script and add the metric Max, Median (more columns if need be) in the array metricNames.
6) Run the script that generates all_metrics.tex


-----------------------------------------------------------------------------------------------------

XXX. LAST THING after re-generating the CSV files. Update online appendix:
- Every Eclipse project, there is a "zipcsv.xml" (it's an Ant build)
- The Ant script will create a zip file that has all the .csv files
(both the LONG and the SHORT output);
- The zip file will be named after the project, e.g., AFS.zip;
- We need to upload the zip file to the online appendix


TOSUM: Analyaze the quantitative data:
- look at statistical significance
- look at LONG output to understand better
- for each new metric, write 1-2 parags. in the same style
as SCAM'14.
-- don't repeat the same info that's in the table
-- NO. for JHD, it's 3. and for HC, it's 7.
-- YES. This metric is statistically significant for all systems, except S1.
We think it's because S1 ... has this characteristic or another.



