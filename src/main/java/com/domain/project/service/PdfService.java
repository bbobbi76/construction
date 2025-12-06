package com.domain.project.service;

import com.domain.project.dto.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * [PDF 서비스]
 * 공사일지 및 안전일지 데이터를 받아 PDF 문서를 생성하는 서비스입니다.
 * 서명 크기 및 가독성을 개선했습니다.
 */
@Service
public class PdfService {

    private static final String FONT_PATH = "c:/Windows/Fonts/Malgun.ttf";

    // =================================================================================
    //                             1. 공사일지 PDF 생성
    // =================================================================================
    public byte[] generateConstructionLogPdf(ConstructionLogDto dto) {
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(bf, 22, Font.BOLD);
            Font sectionFont = new Font(bf, 12, Font.BOLD);
            Font headerFont = new Font(bf, 10, Font.BOLD);
            Font bodyFont = new Font(bf, 10, Font.NORMAL);

            // 1. [제목]
            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setWidthPercentage(100);
            PdfPCell titleCell = new PdfPCell(new Phrase("공 사 작 업 일 지", titleFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setPadding(10);
            titleCell.setBackgroundColor(new BaseColor(245, 245, 245));
            titleTable.addCell(titleCell);
            document.add(titleTable);

            // 2. [상단 정보]
            document.add(new Paragraph("\n"));
            PdfPTable topContainer = new PdfPTable(2);
            topContainer.setWidthPercentage(100);
            topContainer.setWidths(new float[]{6, 4});

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidths(new float[]{3, 7});
            addGrayHeader(infoTable, "현 장 명", headerFont);
            addWhiteCell(infoTable, dto.getLocation(), bodyFont);
            addGrayHeader(infoTable, "일    자", headerFont);
            addWhiteCell(infoTable, dto.getLogDate() + " (" + dto.getWeather() + ")", bodyFont);
            addGrayHeader(infoTable, "공    종", headerFont);
            addWhiteCell(infoTable, dto.getWorkType(), bodyFont);
            addGrayHeader(infoTable, "작 성 자", headerFont);
            addWhiteCell(infoTable, dto.getAuthor(), bodyFont);

            PdfPCell leftCell = new PdfPCell(infoTable);
            leftCell.setBorder(Rectangle.NO_BORDER);
            topContainer.addCell(leftCell);

            // [결재란 수정 시작] -----------------------------------------------------------
            PdfPTable signTable = new PdfPTable(3);
            signTable.setWidths(new float[]{1, 3, 3});
            PdfPCell signTitle = new PdfPCell(new Phrase("결\n재", headerFont));
            signTitle.setRowspan(2);
            signTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            signTitle.setVerticalAlignment(Element.ALIGN_MIDDLE);
            signTable.addCell(signTitle);
            addCellCenter(signTable, "담 당", headerFont);
            addCellCenter(signTable, "소 장", headerFont);

            // 서명 이미지 셀
            PdfPCell signImgCell = new PdfPCell();
            // [수정] 높이를 50f -> 70f로 증가 (칸을 더 넓게)
            signImgCell.setFixedHeight(70f);
            signImgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            signImgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            signImgCell.setPadding(2); // 패딩을 줄여 이미지가 꽉 차게 함

            // [수정] 이미지 허용 크기를 50,40 -> 100,60으로 확대
            addImageToCell(signImgCell, dto.getSignature(), 100, 60);

            signTable.addCell(signImgCell);
            signTable.addCell(new PdfPCell(new Phrase(" ", bodyFont))); // 소장 결재란은 공란

            PdfPCell rightCell = new PdfPCell(signTable);
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setPaddingLeft(5);
            topContainer.addCell(rightCell);
            document.add(topContainer);
            // [결재란 수정 끝] -------------------------------------------------------------

            // 3. [작업 내용]
            document.add(new Paragraph("\n"));
            PdfPTable bodyTable = new PdfPTable(2);
            bodyTable.setWidthPercentage(100);
            bodyTable.setWidths(new float[]{2, 8});

            PdfPCell bHeader = new PdfPCell(new Phrase("■ 금일 주요 작업 내용", sectionFont));
            bHeader.setColspan(2); bHeader.setBorder(Rectangle.NO_BORDER); bHeader.setPaddingBottom(5);
            bodyTable.addCell(bHeader);

            addGrayHeader(bodyTable, "출력 인원", headerFont);
            String wInfo = "총 " + dto.getWorkersCount() + "명";
            if(dto.getWorkerNames()!=null && !dto.getWorkerNames().isEmpty()) {
                wInfo += " (" + String.join(",", dto.getWorkerNames()) + ")";
            }
            addWhiteCell(bodyTable, wInfo, bodyFont);
            addGrayHeader(bodyTable, "작업 상세", headerFont);
            addWhiteCell(bodyTable, dto.getWorkDetails(), bodyFont);
            addGrayHeader(bodyTable, "AI 분석", headerFont);
            addWhiteCell(bodyTable, dto.getAiWorkDescription(), bodyFont);
            document.add(bodyTable);

            // 4. [자재/장비]
            document.add(new Paragraph("\n"));
            PdfPTable resContainer = new PdfPTable(2);
            resContainer.setWidthPercentage(100);
            resContainer.setWidths(new float[]{1, 1});

            PdfPTable eqTable = new PdfPTable(2);
            eqTable.setWidths(new float[]{7, 3});
            addHeaderColored(eqTable, "장비명", headerFont, new BaseColor(230, 240, 255));
            addHeaderColored(eqTable, "대수", headerFont, new BaseColor(230, 240, 255));
            if(dto.getEquipment()!=null) {
                for(EquipmentDto e:dto.getEquipment()){ addWhiteCell(eqTable,e.getName(),bodyFont); addWhiteCellCenter(eqTable,String.valueOf(e.getCount()),bodyFont); }
            } else { addWhiteCell(eqTable,"-",bodyFont); addWhiteCell(eqTable,"-",bodyFont); }
            resContainer.addCell(new PdfPCell(eqTable));

            PdfPTable matTable = new PdfPTable(2);
            matTable.setWidths(new float[]{7, 3});
            addHeaderColored(matTable, "자재명", headerFont, new BaseColor(255, 245, 230));
            addHeaderColored(matTable, "수량", headerFont, new BaseColor(255, 245, 230));
            if(dto.getMaterials()!=null) {
                for(MaterialDto m:dto.getMaterials()){ addWhiteCell(matTable,m.getName(),bodyFont); addWhiteCellCenter(matTable,m.getQuantity(),bodyFont); }
            } else { addWhiteCell(matTable,"-",bodyFont); addWhiteCell(matTable,"-",bodyFont); }
            resContainer.addCell(new PdfPCell(matTable));
            document.add(resContainer);

            // 5. [사진]
            document.add(new Paragraph("\n"));
            PdfPCell pHead = new PdfPCell(new Phrase("■ 현장 작업 사진 대장", sectionFont));
            pHead.setBorder(Rectangle.NO_BORDER); pHead.setPaddingBottom(5);
            PdfPTable ph = new PdfPTable(1); ph.setWidthPercentage(100); ph.addCell(pHead); document.add(ph);

            PdfPTable pg = new PdfPTable(2); pg.setWidthPercentage(100);
            if(dto.getPhotos()!=null && !dto.getPhotos().isEmpty()) {
                for(String p : dto.getPhotos()){
                    PdfPCell c = new PdfPCell();
                    c.setFixedHeight(180f);
                    c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    c.setHorizontalAlignment(Element.ALIGN_CENTER);

                    addImageToCell(c, p, 240, 170);

                    pg.addCell(c);
                }
                if(dto.getPhotos().size()%2!=0) pg.addCell("");
            } else {
                PdfPCell no = new PdfPCell(new Phrase("사진 없음", bodyFont));
                no.setColspan(2); pg.addCell(no);
            }
            document.add(pg);

            // 6. [특이사항]
            document.add(new Paragraph("\n"));
            PdfPTable footer = new PdfPTable(2); footer.setWidthPercentage(100); footer.setWidths(new float[]{2, 8});
            addGrayHeader(footer, "특이사항", headerFont); addWhiteCell(footer, dto.getRemarks(), bodyFont);
            document.add(footer);

            document.close();
        } catch(Exception e){
            throw new RuntimeException("공사일지 PDF 생성 중 오류 발생", e);
        }
        return out.toByteArray();
    }

    // =================================================================================
    //                             2. 안전일지 PDF 생성
    // =================================================================================
    public byte[] generateSafetyLogPdf(SafetyLogDto dto) {
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(bf, 20, Font.BOLD);
            Font sectionFont = new Font(bf, 11, Font.BOLD);
            Font headerFont = new Font(bf, 10, Font.BOLD);
            Font bodyFont = new Font(bf, 9, Font.NORMAL);

            // 1. [제목]
            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setWidthPercentage(100);
            PdfPCell titleCell = new PdfPCell(new Phrase("안전 관리 활동 일지", titleFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setPadding(8);
            titleCell.setBackgroundColor(new BaseColor(245, 245, 245));
            titleTable.addCell(titleCell);
            document.add(titleTable);

            // 2. [기본 정보]
            PdfPTable infoContainer = new PdfPTable(2);
            infoContainer.setWidthPercentage(100);
            infoContainer.setSpacingBefore(8f);
            infoContainer.setWidths(new float[]{6, 4});

            PdfPTable basicInfo = new PdfPTable(2);
            basicInfo.setWidths(new float[]{3, 7});
            addGrayHeader(basicInfo, "현 장 명", headerFont);
            addWhiteCell(basicInfo, dto.getLocation(), bodyFont);
            addGrayHeader(basicInfo, "일    자", headerFont);
            addWhiteCell(basicInfo, dto.getLogDate() + " (" + dto.getWeather() + ")", bodyFont);
            addGrayHeader(basicInfo, "작 성 자", headerFont);
            addWhiteCell(basicInfo, dto.getAuthor(), bodyFont);

            PdfPCell infoWrapper = new PdfPCell(basicInfo);
            infoWrapper.setBorder(Rectangle.NO_BORDER);
            infoContainer.addCell(infoWrapper);

            // [안전일지 결재란 수정 시작] ----------------------------------------------------
            PdfPTable signTable = new PdfPTable(3);
            signTable.setWidths(new float[]{1, 3, 3});
            PdfPCell signLabel = new PdfPCell(new Phrase("결\n재", headerFont));
            signLabel.setRowspan(2);
            signLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
            signLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
            signTable.addCell(signLabel);

            addCellCenter(signTable, "안전관리자", headerFont);
            addCellCenter(signTable, "현장소장", headerFont);

            PdfPCell signImgCell = new PdfPCell();
            // [수정] 높이 증가 45f -> 70f
            signImgCell.setFixedHeight(70f);
            signImgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            signImgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            signImgCell.setPadding(2);

            // [수정] 이미지 허용 크기 확대 65,40 -> 100,60
            addImageToCell(signImgCell, dto.getSignature(), 100, 60);

            signTable.addCell(signImgCell);
            signTable.addCell(new PdfPCell(new Phrase(" ", bodyFont)));

            PdfPCell signWrapper = new PdfPCell(signTable);
            signWrapper.setBorder(Rectangle.NO_BORDER);
            signWrapper.setPaddingLeft(5);
            infoContainer.addCell(signWrapper);
            document.add(infoContainer);
            // [안전일지 결재란 수정 끝] ------------------------------------------------------

            // 3. [작업 및 위험요인]
            PdfPTable contentTable = new PdfPTable(2);
            contentTable.setWidthPercentage(100);
            contentTable.setSpacingBefore(8f);
            contentTable.setWidths(new float[]{2, 8});

            addGrayHeader(contentTable, "금일 작업", headerFont);
            addWhiteCell(contentTable, dto.getWorkDetails(), bodyFont);
            addGrayHeader(contentTable, "위험 요인", headerFont);
            addWhiteCell(contentTable, dto.getPotentialRiskFactors(), bodyFont);
            addGrayHeader(contentTable, "안전 대책", headerFont);
            addWhiteCell(contentTable, dto.getCountermeasures(), bodyFont);
            document.add(contentTable);

            // 4. [사진 대장]
            PdfPTable phHeader = new PdfPTable(1);
            phHeader.setWidthPercentage(100);
            phHeader.setSpacingBefore(8f);
            PdfPCell phCell = new PdfPCell(new Phrase("■ 위험 요인 및 조치 결과", sectionFont));
            phCell.setBorder(Rectangle.NO_BORDER);
            phCell.setPaddingBottom(4);
            phHeader.addCell(phCell);
            document.add(phHeader);

            PdfPTable photos = new PdfPTable(2);
            photos.setWidthPercentage(100);

            PdfPCell h1 = new PdfPCell(new Phrase("지적 사항 (Before)", headerFont));
            h1.setBackgroundColor(new BaseColor(255, 235, 235));
            h1.setHorizontalAlignment(Element.ALIGN_CENTER); h1.setPadding(5);
            photos.addCell(h1);

            PdfPCell h2 = new PdfPCell(new Phrase("조치 결과 (After)", headerFont));
            h2.setBackgroundColor(new BaseColor(235, 245, 255));
            h2.setHorizontalAlignment(Element.ALIGN_CENTER); h2.setPadding(5);
            photos.addCell(h2);

            float photoH = 140f;

            PdfPCell riskCell = new PdfPCell();
            riskCell.setFixedHeight(photoH);
            riskCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            riskCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            String photoUrl = (dto.getPhotos() != null && !dto.getPhotos().isEmpty()) ? dto.getPhotos().get(0) : null;
            addImageToCell(riskCell, photoUrl, 200, 130);
            photos.addCell(riskCell);

            PdfPCell actCell = new PdfPCell();
            actCell.setFixedHeight(photoH);
            actCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            actCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            addImageToCell(actCell, dto.getFollowUpPhoto(), 200, 130);
            photos.addCell(actCell);

            document.add(photos);

            // 5. [체크리스트]
            PdfPTable chkHeader = new PdfPTable(1);
            chkHeader.setWidthPercentage(100);
            chkHeader.setSpacingBefore(8f);
            PdfPCell chkTitle = new PdfPCell(new Phrase("■ 안전 점검표", sectionFont));
            chkTitle.setBorder(Rectangle.NO_BORDER); chkTitle.setPaddingBottom(4);
            chkHeader.addCell(chkTitle);
            document.add(chkHeader);

            PdfPTable checklist = new PdfPTable(new float[]{1, 6, 2,  1, 6, 2});
            checklist.setWidthPercentage(100);

            addGrayHeaderCenter(checklist, "NO", headerFont);
            addGrayHeaderCenter(checklist, "점검 항목", headerFont);
            addGrayHeaderCenter(checklist, "결과", headerFont);
            addGrayHeaderCenter(checklist, "NO", headerFont);
            addGrayHeaderCenter(checklist, "점검 항목", headerFont);
            addGrayHeaderCenter(checklist, "결과", headerFont);

            List<SafetyCheckItemDto> items = dto.getSafetyChecklist();
            if (items != null && !items.isEmpty()) {
                int size = items.size();
                for (int i = 0; i < size; i += 2) {
                    addCellCenter(checklist, String.valueOf(i + 1), bodyFont);
                    PdfPCell item1 = new PdfPCell(new Phrase(" " + items.get(i).getItem(), bodyFont));
                    item1.setVerticalAlignment(Element.ALIGN_MIDDLE); item1.setPadding(5);
                    checklist.addCell(item1);
                    addCellCenter(checklist, items.get(i).getStatus(), bodyFont);

                    if (i + 1 < size) {
                        addCellCenter(checklist, String.valueOf(i + 2), bodyFont);
                        PdfPCell item2 = new PdfPCell(new Phrase(" " + items.get(i+1).getItem(), bodyFont));
                        item2.setVerticalAlignment(Element.ALIGN_MIDDLE); item2.setPadding(5);
                        checklist.addCell(item2);
                        addCellCenter(checklist, items.get(i+1).getStatus(), bodyFont);
                    } else {
                        checklist.addCell(""); checklist.addCell(""); checklist.addCell("");
                    }
                }
            } else {
                PdfPCell empty = new PdfPCell(new Phrase("점검 항목 없음", bodyFont));
                empty.setColspan(6); checklist.addCell(empty);
            }
            document.add(checklist);

            // 6. [특이사항]
            PdfPTable footer = new PdfPTable(2);
            footer.setWidthPercentage(100);
            footer.setSpacingBefore(8f);
            footer.setWidths(new float[]{2, 8});
            addGrayHeader(footer, "특이사항", headerFont);
            PdfPCell rmk = new PdfPCell(new Phrase(dto.getRemarks(), bodyFont));
            rmk.setVerticalAlignment(Element.ALIGN_MIDDLE); rmk.setPadding(5);
            footer.addCell(rmk);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("안전일지 PDF 생성 중 오류 발생", e);
        }
        return out.toByteArray();
    }

    // =================================================================================
    //                             3. 유틸리티 메서드 (Helper)
    // =================================================================================
    private void addImageToCell(PdfPCell cell, String imagePath, float fitWidth, float fitHeight) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        try {
            Image img = getLocalImage(imagePath);
            if (img != null) {
                // 이미지가 셀 크기에 맞춰지도록 스케일 조정 (비율 유지)
                img.scaleToFit(fitWidth, fitHeight);
                cell.addElement(img);
            }
        } catch (Exception e) {
            System.err.println("PDF 이미지 추가 실패: " + imagePath);
        }
    }

    private Image getLocalImage(String webPath) {
        if (webPath == null || webPath.isEmpty()) return null;
        try {
            String localPath = webPath.startsWith("/") ? webPath.substring(1) : webPath;
            if (Files.exists(Paths.get(localPath))) return Image.getInstance(localPath);
        } catch (Exception e) {
            System.err.println("로컬 이미지 로드 예외: " + e.getMessage());
        }
        return null;
    }

    // --- (단순 반복 헬퍼) ---
    private void addGrayHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(240, 240, 240));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }
    private void addHeaderColored(PdfPTable table, String text, Font font, BaseColor color) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(color);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }
    private void addGrayHeaderCenter(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(240, 240, 240));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }
    private void addWhiteCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }
    private void addWhiteCellCenter(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }
    private void addCellCenter(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }
}