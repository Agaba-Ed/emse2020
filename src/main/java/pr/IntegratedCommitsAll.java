package pr;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import core.Call_URL;
import core.Create_Excel;
import core.CsvWriter;
import core.DateOperations;
import core.JSONUtils;
import git.CommitMetadata;
import git.GitCherryPickedCommits;
import git.GitMergedRebasedCommits;
import git.UniqueCommits;
import picks.Pick_GeneralNext;
import util.Constants;

public class IntegratedCommitsAll implements Runnable {

	int task, start, end, ct;
	String file = "";

	IntegratedCommitsAll(int start, int end, int task, int ct) {
		this.end = end;
		this.start = start;
		this.task = task;
		this.ct = ct;
	}

	public void run() {

//		int ct = 0;
		String fileOld = "/Users/businge/Documents/000emse2020/00Dataset/npm_variants_1.xlsx";
//		String file = "/Users/businge/Documents/000emse2020/00Dataset/npm/npm_integration_1_" + task + ".xlsx";
//		String file1 = "/Users/businge/Documents/000emse2020/00Dataset/npm_fv_to_mlv/npm_integration_" + task + ".csv";
		String file1 = "/Users/businge/Documents/000emse2020/00Dataset/npm_mlv_to_fv/npm_integration_" + task + ".csv";
		
		Object[] datas = null;
		Object[] numbers = null;
		ArrayList<Object[]> allobjD = new ArrayList<Object[]>();
		ArrayList<Object[]> allobjN = new ArrayList<Object[]>();

		try {
//			pick(String file, int sheetNum, int cellNumber, int rowNum)
			List<String> mlv = Pick_GeneralNext.pick(fileOld, 0, 0, 1);
			List<String> fv = Pick_GeneralNext.pick(fileOld, 0, 1, 1);

			List<String> forkDate = Pick_GeneralNext.pick(fileOld, 0, 2, 1);
			List<String> mlvDate = Pick_GeneralNext.pick(fileOld, 0, 3, 1);
			List<String> fvDate = Pick_GeneralNext.pick(fileOld, 0, 4, 1);


			datas = new Object[] { "MLV", "FV", "ForkDate", "MLVLastComDate", "FVLastComDate", "StartingCommits",
					"AllCommitsAFD", "NumberMergedCom", "NumberUnqCom", "cpstatus", "UniqueCommits", "Origin" };
			allobjD.add(datas);
			
//			numbers = new Object[] { mlv.get(i1), fv.get(i1), forkDate.get(i1), mlvDate.get(i1), fvDate.get(i1),
//					totMerged, totMergedCom, totSquashed, totSquashedCom, totRebased, totRebasedCom, totUnknown, totUnknownCom,
//					prIntegratedCommitsVar1.size(), prIntegratedCommitsVar2.size(),
//					gitCommitMergeRebase.size(), gitCommitMergeRebase.size()};
			
			numbers = new Object[] {"No", "MLV", "FV", "ForkDate", "MLVLastComDate", "FVLastComDate",
					"PR_Mg", "PR_Mg_Com", "PR_Sq", "PR_Sq_Com", "PR_Rb", "PR_Rb_Com", "PR_Uk", "PR_Uk_Com", 
					"PR_Inter_Com_Var1", "PR_Inter_Com_Var2", "Git_MgRb_Com", "Git_Cherry_Com", "Unique_Var1", "Unique_Var2"};
			allobjN.add(numbers);

			//

			for (int i1 = start; i1 < end; i1++) {
//				this is for integration from mlv to fv
				String variant2 = mlv.get(i1);
				String variant1 = fv.get(i1);
				System.out.println("Task-"+task+ " "+ i1 + " -- " + variant2 + " : " + variant1);
				
//				this is for integration from fv to mlv
//				String variant2 = fv.get(i1);
//				String variant1 = mlv.get(i1);
//				
//				System.out.println("Task-"+task+ " "+ i1 + " -- " + variant1 + " : " + variant2);
				String startDate = forkDate.get(i1);
				
//				System.out.println(mlvDate.get(i1));
//				System.out.println(fvDate.get(i1));
				String dateComp = DateOperations.dates(mlvDate.get(i1), fvDate.get(i1));
				String stopDate = "";

				if (dateComp.equals("true"))
					stopDate = mlvDate.get(i1);
				else
					stopDate = fvDate.get(i1);
				List<String> prMerged = new ArrayList<String>();
				List<String> prMergedCom = new ArrayList<String>();
				List<String> prRebased = new ArrayList<String>();
				List<String> prRebasedCom = new ArrayList<String>();
				List<String> prSquashed = new ArrayList<String>();
				List<String> prSquashedCom = new ArrayList<String>();
				List<String> prUnknown = new ArrayList<String>();
				List<String> prUnknownCom = new ArrayList<String>();
				
				List<String> prIntegratedCommitsVar1 = new ArrayList<String>();
				List<String> prIntegratedCommitsVar2 = new ArrayList<String>();
				List<String> gitCommitCherryVar1 = new ArrayList<String>();
				List<String> gitCommitCherryVar2 = new ArrayList<String>();
				List<String> cherryPickSource = new ArrayList<String>();

				List<String> gitCommitMergeRebase = new ArrayList<String>();

				List<String> uniqueCommitsVar1 = new ArrayList<String>();
				List<String> uniqueCommitsVar2 = new ArrayList<String>();

				// These are commits in a variant after the forkdate exlcuding the PR commits
				// and any "PR merge_commit"
				List<String> allCommitsAfterForkDateVar1 = new ArrayList<String>();
				List<String> allCommitsAfterForkDateVar2 = new ArrayList<String>();

				List<String> prDetails = integratedPR(variant1, variant2, startDate, stopDate, Constants.getToken(),
						ct);
				String integratedPr = "";
				ct = Integer.parseInt(prDetails.get(prDetails.size() - 1));
				String[] ret = {};
				int totMerged = 0, totSquashed = 0, totRebased = 0, totUnknown = 0;
				int totMergedCom = 0, totSquashedCom = 0, totRebasedCom = 0, totUnknownCom = 0;
				for (int i = 0; i < prDetails.size() - 1; i++) {
					String[] prDetail = prDetails.get(i).split("===");
					String merge_commit_sha = prDetail[1], PR_Number = prDetail[2], prMergeDate = prDetail[4];
					integratedPr = PropagatedPR.propagatedPR(variant1, merge_commit_sha, prMergeDate,
							Constants.getToken(), ct);
					ret = integratedPr.split("=");
					ct = Integer.parseInt(ret[1]);

					ArrayList<String> prCom = prCommitSize(variant1, PR_Number, Constants.getToken(), ct);

					ct = Integer.parseInt(prCom.get(prCom.size() - 1));
					prCom.remove(prCom.size() - 1);
					int prComSize = prCom.size();
					prIntegratedCommitsVar2.addAll(prCom);
					String interCom = integratedCommitSize(variant1, prMergeDate, Constants.getToken(), ct);

					String[] str1 = interCom.split("=");
					ct = Integer.parseInt(str1[1]);
					int interComSize = Integer.parseInt(str1[0]);

					if (ct == (Constants.getToken().length)) {// the the index for the tokens array...
						ct = 0; // go back to the first index......
					}

					// Squashed PR Commits
					if (ret[0].equals("true") && interComSize == 1) {
//						System.out.println("Squashed PR");
//						System.out.println("PR-" + PR_Number + " : MergeCommit-" + merge_commit_sha.substring(0, 8)
//								+ " : MergeDate-" + prMergeDate + " : #Commits = " + prComSize + " : Squashed");
						// ArrayList<String> prCommits = PullRequestCommits.pullRequestCommits(variant1,
						// PR_Number,
						// Constants.getToken(), ct);
						// String[] strSq = prCommits.get(prCommits.size() - 1).split("=");
						// String prCommitsSq = strSq[0];
						// ct = Integer.parseInt(strSq[1]);
						// prCommits.remove(prCommits.size() - 1);
//						System.out.println("SquashedCommit | PRCommits");
//						System.out.println(merge_commit_sha.substring(0, 8) + " | " + prCom + "\n");
						totSquashedCom += prComSize;
						totSquashed++;
						prIntegratedCommitsVar1.add(merge_commit_sha.substring(0, 8));
					}
					// Rebased PR commits
					else if (ret[0].equals("true") && interComSize > 1) {
//						System.out.println("Rebased PR");
//						System.out.println("PR-" + PR_Number + " : MergeCommit-" + merge_commit_sha.substring(0, 8)
//								+ " :  MergeDate-" + prMergeDate + " : #Commits = " + prComSize + " Rebased");
						ArrayList<String> rebasedCom1 = RebasedPR.rebasedPR(variant1, prMergeDate, PR_Number,
								Constants.getToken(), ct);
						String[] str11 = rebasedCom1.get(rebasedCom1.size() - 1).split("=");
						ct = Integer.parseInt(str11[1]);
						String rebasedCommits = str11[0];
						rebasedCom1.remove(rebasedCom1.size() - 1);
//						System.out.println("|prCommit : baseRepoCommit| -- " + rebasedCommits + "\n");
						totRebasedCom += prComSize;
						totRebased++;
						prIntegratedCommitsVar1.addAll(rebasedCom1);
						// Merged PR Commits
					} else if (ret[0].equals("false")) {
						if (ct == (Constants.getToken().length)) {// the the index for the tokens array...
							ct = 0; // go back to the first index......
						}
						integratedPr = MergedPR.mergedPR(variant1, merge_commit_sha, prMergeDate, Constants.getToken(),
								ct);
						ret = integratedPr.split("=");
						ct = Integer.parseInt(ret[1]);
						if (ret[0].equals("true")) {
							// ArrayList<String> prCommits = PullRequestCommits.pullRequestCommits(variant1,
							// PR_Number,
							// Constants.getToken(), ct);
							// String[] strmg = prCommits.get(prCommits.size() - 1).split("=");
							// String prCommitsmg = strmg[0];
							// ct = Integer.parseInt(strmg[1]);
							// prCommits.remove(prCommits.size() - 1);
//							System.out.println("Merged PR");
//							System.out.println("PR-" + PR_Number + " : MergeCommit-" + merge_commit_sha.substring(0, 8)
//									+ " :  MergeDate-" + prMergeDate + " : #Commits = " + prComSize + " : Merged");
//
//							System.out.println("Merged Commits | " + prCom + "\n");
							totMergedCom += prComSize;
							totMerged++;
							prIntegratedCommitsVar1.addAll(prCom);
							// Unidentified PR integrated commits
						} else {
							ArrayList<String> prRebasedCommits = new ArrayList<String>();
							String[] missMgC = MergedPRMissingMergeCommit.missingMergeCommit(variant1, PR_Number,
									prMergeDate, prRebasedCommits, Constants.getToken(), ct).split("=");
							String prCommitsmg = missMgC[0];
							ct = Integer.parseInt(missMgC[1]);
							if (prRebasedCommits.size() == 1 && prCommitsmg.equals("merged")) {
//								System.out.println("Merged PR; Missing Merge Commit");
//								System.out.println("PR-" + PR_Number + " : MergeCommit-"
//										+ merge_commit_sha.substring(0, 8) + " :  MergeDate-" + prMergeDate
//										+ " : #Commits = " + prComSize + " : Merged");
//
//								System.out.println("Merged Commits | " + prCom + "\n");
								totMergedCom += prComSize;
								totMerged++;
								prIntegratedCommitsVar1.addAll(prCom);
							} else if (prRebasedCommits.size() > 1 && prCommitsmg.equals("rebased")) {
//								System.out.println("Rebased PR; Rebase time Different");
//								System.out.println("PR-" + PR_Number + " : MergeCommit-"
//										+ merge_commit_sha.substring(0, 8) + " :  MergeDate-" + prMergeDate
//										+ " : #Commits = " + prComSize + " : Rebased");
//
//								System.out.println("|prCommit : baseRepoCommit| -- "
//										+ prRebasedCommits.get(prRebasedCommits.size() - 1) + "\n");
								prRebasedCommits.remove(prRebasedCommits.size() - 1);
								prIntegratedCommitsVar1.addAll(prRebasedCommits);
								totRebasedCom += prComSize;
								totRebased++;
							} else {
//								System.out.println("Unidentified PR Integration");
//								System.out.println("PR-" + PR_Number + " : MergeCommit-"
//										+ merge_commit_sha.substring(0, 8) + " :  MergeDate-" + prMergeDate
//										+ " : #Commits = " + prComSize + " : Unknown!!!!! \n");

								prIntegratedCommitsVar1.addAll(prCom);
								totUnknownCom += prComSize;
								totUnknown++;
							}
						}
					}
				}
				// Git integrated commits
				List<String> var1CommitMetadata = CommitMetadata.metadata(variant1, prIntegratedCommitsVar1, startDate,
						stopDate, allCommitsAfterForkDateVar1, Constants.getToken(), ct);
				ct = Integer.parseInt(var1CommitMetadata.get(var1CommitMetadata.size() - 1));
				var1CommitMetadata.remove(var1CommitMetadata.size() - 1);

				List<String> var2CommitMetadata = CommitMetadata.metadata(variant2, prIntegratedCommitsVar2, startDate,
						stopDate, allCommitsAfterForkDateVar2, Constants.getToken(), ct);
				ct = Integer.parseInt(var2CommitMetadata.get(var2CommitMetadata.size() - 1));
				var2CommitMetadata.remove(var2CommitMetadata.size() - 1);

				GitMergedRebasedCommits.gitMergedRebasedCommits(var1CommitMetadata, var2CommitMetadata,
						gitCommitMergeRebase);

				cherryPickSource = GitCherryPickedCommits.gitCherryPickedCommits(variant1, variant2, var1CommitMetadata,
						var2CommitMetadata, gitCommitCherryVar1, gitCommitCherryVar2, Constants.getToken(), ct);
				ct = Integer.parseInt(cherryPickSource.get(cherryPickSource.size() - 1));
				cherryPickSource.remove(cherryPickSource.size() - 1);

				uniqueCommitsVar1 = UniqueCommits.repoUniqueCommits(allCommitsAfterForkDateVar1, gitCommitMergeRebase,
						gitCommitCherryVar1, gitCommitCherryVar2);
				uniqueCommitsVar2 = UniqueCommits.repoUniqueCommits(allCommitsAfterForkDateVar2, gitCommitMergeRebase,
						gitCommitCherryVar1, gitCommitCherryVar2);

				System.out.println(" PR Integrated Commits");
				System.out.println("#MergedPR = " + totMerged + " | #MergedPRCommits = " + totMergedCom
						+ " \n#SquashedPR = " + totSquashed + " | #SquashedPRCommits = " + totSquashedCom
						+ "\n#RebasedPR = " + totRebased + " | #RebasedPRCommits = " + totRebasedCom
						+ " \n#Uclassified PR Integration = " + totUnknown + " | Commits = " + totUnknownCom);

				System.out.println("\nprIntegratedCommitsVariant1_size = " + prIntegratedCommitsVar1.size());
				System.out.println("prIntegratedCommitsVariant2_size = " + prIntegratedCommitsVar2.size());
				System.out.println("prIntegratedCommitsVariant1 = " + prIntegratedCommitsVar1.toString());
				System.out.println("prIntegratedCommitsVariant2 = " + prIntegratedCommitsVar2.toString());

				System.out.println("\n Git Integrated Commits ");
				System.out.println("gitCommitMergeRebase = " + gitCommitMergeRebase.toString());
				System.out.println("gitCommitMergeRebase_size = " + gitCommitMergeRebase.size());

				System.out.println("\ngitCommitCherryPick_size : " + gitCommitCherryVar1.size());
				if (gitCommitCherryVar1.size() != 0) {
					System.out.println("variant1-Commit : variant2-Commit : Cherrypick-Source-Repo");
					String cherry = "| ";
					for (int i = 0; i < gitCommitCherryVar1.size(); i++) {
						cherry += gitCommitCherryVar1.get(i) + ":" + gitCommitCherryVar2.get(i) + ":"
								+ cherryPickSource.get(i) + " | ";
					}
					System.out.println(cherry);
				}

				System.out.println("\n Unique Commits");
				System.out.println("uniqueCommitsVariant1_size = " + uniqueCommitsVar1.size());
				System.out.println("uniqueCommitsVariant2_size = " + uniqueCommitsVar2.size());
				
				numbers = new Object[] {i1, mlv.get(i1), fv.get(i1), forkDate.get(i1), mlvDate.get(i1), fvDate.get(i1),
						totMerged, totMergedCom, totSquashed, totSquashedCom, totRebased, totRebasedCom, totUnknown, totUnknownCom,
						prIntegratedCommitsVar1.size(), prIntegratedCommitsVar2.size(),
						gitCommitMergeRebase.size(), gitCommitCherryVar1.size(), uniqueCommitsVar1.size(), uniqueCommitsVar2.size()};
				allobjN.add(numbers);
				
//				datas = new Object[] { mlv.get(i1), fv.get(i1), forkDate.get(i1), mlvDate.get(i1), fvDate.get(i1),
//						totMerged, totMergedCom, totSquashed, totSquashedCom, totRebased, totRebasedCom, totUnknown, totUnknownCom,
//						prIntegratedCommitsVar1.size(), prIntegratedCommitsVar2.size(),
//						gitCommitMergeRebase.size(), gitCommitCherryVar1.size(), uniqueCommitsVar1.size(), uniqueCommitsVar2.size()};
//				allobjD.add(numbers);

				
				CsvWriter.csvWriter(allobjN, file1);
				System.out.println();
//				String sheetName = "Git_Integration";
//				Create_Excel.createExcel2(allobjN, 1, file, sheetName);
//				String sheetName = "Git_N";
//				Create_Excel.createExcel2(allobjD, 1, file1, sheetName);
				
				
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/*
	 * This Method the mines integrated pull requests
	 * 
	 * @variant1 is the destination repository [head]
	 * 
	 * @variant2 is the source repository [base]
	 * 
	 * @stopDate we are interested in pr that are integrated before this stopping
	 * date
	 * 
	 * @token an array of GitHub tokens
	 * 
	 * @ct counter for the tokens
	 * 
	 * @return: ArrayList of "fullname + ":" + merge_commit_sha + ":" + PR_Number"
	 * Last item in the list is the token counter
	 */
	public static List<String> integratedPR(String variant1, String variant2, String startDate, String stopDate,
			String[] token, int ct) {
		List<String> prDetails = new ArrayList<String>();

		JSONParser parser = new JSONParser();
		int p = 1;

		try {
			// control to break out of the infinite loop
			while (true) {
				if (ct >= (token.length)) {// the the index for the tokens array...
					ct = 0; // go back to the first index......
				}

//				String mlv_url = Call_URL.callURL("https://api.github.com/repos/" + variant1 + "/pulls?page=" + p
//						+ "&per_page=100&state=closed&access_token="+ token[ct++]);
				String mlv_url = Call_URL.callURL1(
						"https://api.github.com/repos/" + variant1 + "/pulls?page=" + p + "&per_page=100&state=closed",
						token[ct]);
				ct++;
//				System.out.println("https://api.github.com/repos/" + variant1 + "/pulls?page=" + p + "&per_page=100&state=closed&access_token="+ token[ct]);
				if (JSONUtils.isValidJSON(mlv_url) == false) {///

					System.out.println("NNNN :Invalid Pull found!");
					break;
				}
				JSONArray jsonArry = (JSONArray) parser.parse(mlv_url);
				if (jsonArry.toString().equals("[]")) {
					break;
				}
				for (Object o : jsonArry) {

					JSONObject jsonObject = (JSONObject) o;

					String fullname = "";
					String merge_commit_sha = "";
					String created_at = "";
					String merged_at = "";
					long PR_Number = 0;
					if (jsonObject.get("merged_at") != null) {

						JSONObject baseOBJ = (JSONObject) jsonObject.get("head");
						JSONObject reposObject = (JSONObject) baseOBJ.get("repo");

						if (reposObject != null) {
							fullname = (String) reposObject.get("full_name");
							merge_commit_sha = (String) jsonObject.get("merge_commit_sha");
							created_at = (String) jsonObject.get("created_at");
							merged_at = (String) jsonObject.get("merged_at");
							PR_Number = (Long) jsonObject.get("number");

							// remove this after

							String dateCompStop = DateOperations.dates(stopDate, merged_at);
							String dateCompStart = DateOperations.dates(startDate, merged_at);

							if (dateCompStop.equals("true") && dateCompStart.equals("false")) {

								if (fullname.equals(variant2)) {
									// System.out.println(fullname + " : " + merge_commit_sha + " : " + PR_Number);
									prDetails.add(fullname + "===" + merge_commit_sha + "===" + PR_Number + "==="
											+ created_at + "===" + merged_at);
								}
							}
						}
					}
				}
				p++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		prDetails.add("" + ct);
		return prDetails;
	}

	public static String integratedCommitSize(String repo, String prMergeDate, String[] tokens, int ct) {

		String since = DateOperations.dateLessBy1Sec(prMergeDate);

		if (ct >= (tokens.length)) {// the the index for the tokens array...
			ct = 0; // go back to the first index......
		}

		String repo_url = Call_URL.callURL1(
				"https://api.github.com/repos/" + repo + "/commits?since=" + since + "&until=" + prMergeDate,
				tokens[ct]);
		ct++;

		JSONParser parser = new JSONParser();
		JSONArray jsonArray = null;
		try {
			jsonArray = (JSONArray) parser.parse(repo_url);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return jsonArray.size() + "=" + ct;
	}

	public static ArrayList<String> prCommitSize(String repo, String prNumber, String[] tokens, int ct) {
		ArrayList<String> prComList = new ArrayList<String>();
		if (ct >= (tokens.length)) {// the the index for the tokens array...
			ct = 0; // go back to the first index......
		}

		String repo_url = Call_URL.callURL1("https://api.github.com/repos/" + repo + "/pulls/" + prNumber + "/commits",
				tokens[ct]);
		ct++;

		JSONParser parser = new JSONParser();
		JSONArray jsonArray = null;

		try {
			jsonArray = (JSONArray) parser.parse(repo_url);
			for (int i = jsonArray.size() - 1; i >= 0; i--) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);

				String prCommitSha = (String) jsonObject.get("sha");
				prComList.add(prCommitSha.substring(0, 8));
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		prComList.add("" + ct);
		return prComList;
	}

}
