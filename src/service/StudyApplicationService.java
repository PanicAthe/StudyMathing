package service;

import java.util.List;

import dao.StudyApplicationDAO;
import dao.StudyDAO;
import dto.StudyApplicationDTO;
import dto.StudyDTO;

public class StudyApplicationService {
	
	private final StudyDAO studyDAO = new StudyDAO();
	private final StudyApplicationDAO applicationDAO = new StudyApplicationDAO();
	
	public List<StudyApplicationDTO> listMyApplications(int userId) {
		return applicationDAO.findApplicationsByUserId(userId);
	}
	
	public boolean applyToStudy(int studyId, int userId, String selfIntro) {
		StudyDTO study = studyDAO.findById(studyId);
		if (study == null || study.getIsActive()==0)
			return false;
		if (applicationDAO.existsApplication(studyId, userId))
			return false;

		applicationDAO.insertApplication(StudyApplicationDTO.builder().studyId(studyId).userId(userId).status(0)
				.selfIntroduction(selfIntro).build());
		return true;
	}
	
	public List<StudyApplicationDTO> listApplicationsForStudy(int studyId) {
		return applicationDAO.findApplicationsByStudyId(studyId);
	}
	
	public boolean handleApplication(int applicationId, int decision) {
		StudyApplicationDTO app = applicationDAO.findById(applicationId);
		if (app == null || app.getStatus() != 0)
			return false;

		applicationDAO.updateApplicationStatus(applicationId, decision);
		return true;
	}
	
	public boolean isUserJoinedStudy(int userId, int studyId) {
		return applicationDAO.existsAcceptedApplication(userId, studyId);
	}
	
	public boolean leaveStudy(int userId, int studyId) {
		if (!applicationDAO.existsAcceptedApplication(userId, studyId))
			return false;
		applicationDAO.deleteApplication(userId, studyId);
		return true;
	}


}
