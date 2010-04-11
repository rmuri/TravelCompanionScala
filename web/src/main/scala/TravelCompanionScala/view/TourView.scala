package TravelCompanionScala.view

import TravelCompanionScala.model.UserManagement
import TravelCompanionScala.snippet.TourEnum
import net.liftweb.http.{S, LiftView}
import xml.{Text, NodeSeq}

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 09.04.2010
 * Time: 14:07:41
 * To change this template use File | Settings | File Templates.
 */


class TourView extends LiftView {
  override def dispatch = {
    case "edit" => doEdit _
    case "view" => doView _
    case _ => doShow _
  }

  def doView(): NodeSeq = {
    <lift:surround with="default" at="content">
      <lift:TourSnippet.viewTour>
        <h1>
            <tour:name/>
        </h1>
        <span>
            <tour:description/>
        </span>
      </lift:TourSnippet.viewTour>
    </lift:surround>

  }

  def doEdit(): NodeSeq = {
    <lift:surround with="default" at="content">
      <p>
        Not yet implemented...
      </p>
    </lift:surround>
  }

  def doShow(): NodeSeq = {
    <lift:surround with="default" at="content">
      {if (UserManagement.loggedIn_?) {
      <h1>
        Ihre Reisen
      </h1>
              <span>
                Nachfolgend erhalten Sie eine Liste der von Ihnen eingetragenen Reisen:
              </span>

              <table>
                <thead>
                  <tr>
                    <th>
                      Name
                    </th>
                    <th>
                      Description
                    </th>
                    <th>
                      Operations
                    </th>
                  </tr>
                </thead>

                <tbody>
                  <lift:TourSnippet.listTours which="OwnTours">
                    <tr>
                      <td>
                          <tour:name/>
                      </td>
                      <td>
                          <tour:description/>
                      </td>
                      <td>
                        <a tour:view_href=" ">View
                        </a>
                      </td>

                    </tr>
                  </lift:TourSnippet.listTours>
                </tbody>
              </table>

              <h1>
                Reisen anderer Mitglieder
              </h1>
              <span>
                Nachfolgend erhalten Sie eine Liste der von anderen Mitgliedern eingetragenen Reisen:
              </span>
              <table>
                <thead>
                  <tr>
                    <th>
                      Name
                    </th>
                    <th>
                      Description
                    </th>
                    <th>
                      Creator
                    </th>
                  </tr>
                </thead>

                <tbody>
                  <lift:TourSnippet.listTours which="OthersTours">
                    <tr>
                      <td>
                          <tour:name/>
                      </td>
                      <td>
                          <tour:description/>
                      </td>
                      <td>
                          <tour:creator/>
                      </td>
                    </tr>
                  </lift:TourSnippet.listTours>
                </tbody>
              </table>
    } else {
      <h1>
        Eingetragene Reisen
      </h1>
              <span>
                Nachfolgend erhalten Sie eine Liste der von beliebigen Mitgliedern eingetragenen Reisen:
              </span>
              <table>
                <thead>
                  <tr>
                    <th>
                      Name
                    </th>
                    <th>
                      Description
                    </th>
                  </tr>
                </thead>

                <tbody>
                  <lift:TourSnippet.listTours which="AllTours">
                    <tr>
                      <td>
                          <tour:name/>
                      </td>
                      <td>
                          <tour:description/>
                      </td>

                    </tr>
                  </lift:TourSnippet.listTours>
                </tbody>
              </table>
    }}
    </lift:surround>
  }
}