import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient.Expandos;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class JRJC {

  private static final String SAMPLE_CSV_FILE = "./sample.csv";
  private static final List<Expandos> expand = Arrays.asList(Expandos.CHANGELOG);
  private static IssueRestClient issueClient;
  private static SearchRestClient searchClient;

  private static CSVPrinter csvPrinter;

  private static void setup() throws URISyntaxException, IOException {
    URI serverUri = new URI("https://issues.apache.org/jira");
    JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();

    JiraRestClient restClient = factory.createWithBasicHttpAuthentication(serverUri, "jcheruve", "123oiu");
    issueClient = restClient.getIssueClient();
    searchClient = restClient.getSearchClient();

    BufferedWriter writer = Files.newBufferedWriter(Paths.get(SAMPLE_CSV_FILE));
    csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("ID", "history", "reopenings", "score"));
  }

  public static void main(String[] args) throws Exception {

    setup();
    SSWrapper ssWrapper = new SSWrapper();
    Promise<SearchResult> search = searchClient.searchJql("project = ZOOKEEPER ORDER BY created DESC", 200, 0);
    runSearch(search, ssWrapper);
    search = searchClient.searchJql("project = QPID ORDER BY created DESC", 200, 0);
    runSearch(search, ssWrapper);
    search = searchClient.searchJql("project = CRUNCH ORDER BY created DESC", 200, 0);
    runSearch(search, ssWrapper);
    search = searchClient.searchJql("project = COCOON ORDER BY created DESC", 200, 0);
    runSearch(search, ssWrapper);
    search = searchClient.searchJql("project = CLOUDSTACK ORDER BY created DESC", 200, 0);
    runSearch(search, ssWrapper);
  }


  private static void runSearch(Promise<SearchResult> search, SSWrapper ssWrapper) throws Exception {
    int i = 0;
    for (Issue issue: search.claim().getIssues()) {

      String changes = "";
      String comments = "";
      int reopenings = 0;

      if(i > 200) {
        break;
      }

      System.out.println(issue.getKey());

      issue = issueClient.getIssue(issue.getKey(), expand).claim();
      for (Comment comment : issue.getComments()) {
        comments += comment;
      }

      Iterable<ChangelogGroup> changelog = issue.getChangelog();

      if(changelog == null) {
        continue;
      }

      for (ChangelogGroup group: changelog) {
        Iterable<ChangelogItem> clItems = group.getItems();
        for (ChangelogItem clItem: clItems) {

          if(clItem.getField().equals("status")) {
            changes += "Date: "+ group.getCreated()
                + ", From: " + clItem.getFromString() + " To: " + clItem.getToString();
            if(clItem.getToString().equals("Open") || clItem.getToString().equals("Reopened")) {
              reopenings++;
            }
          }
        }
      }

      String processed = ssWrapper.process(comments);
      String scored = ssWrapper.compute(processed);
      Scanner scan = new Scanner(scored);
      int num1 = scan.nextInt();
      int num2 = scan.nextInt();

      i++;
      csvPrinter.printRecord(issue.getKey(), changes, reopenings, num1 + ", " + num2);
    }

    csvPrinter.flush();
    System.out.println("done!");

  }

}
