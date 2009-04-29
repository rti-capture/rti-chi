p = 1;
for ii = 1:n_ima
if active_images(ii)
    for jj = 1:n_ima
    if active_images(jj) && (ii~=jj)
	    eval(['omc_ii = omc_' num2str(ii) ';']);
	    eval(['Tc_ii = Tc_' num2str(ii) ';']);
	    eval(['omc_jj = omc_' num2str(jj) ';']);
	    eval(['Tc_jj = Tc_' num2str(jj) ';']);

        XX = [100 0  0 10 20 20 40 50 30 50 90 30  5 40 35 35 70 90;
              0 100  0 20 10 20 50 40 50 30 60 60 25 25 35 80 80 70;
              0   0  0  0  0 80 80  0 10 10 30 90 40  5 80 80 90 80];

        YY_T1 = rodrigues(omc_ii) * XX + Tc_ii * ones(1,length(XX));
        YY_T2 = rodrigues(omc_jj) * XX + Tc_jj * ones(1,length(XX));
        YY_1 = [YY_T1(1,:); YY_T1(3,:); -YY_T1(2,:)];  
        YY_2 = [YY_T2(1,:); YY_T2(3,:); -YY_T2(2,:)];  

%         sB = zeros(3,3);
%         for ik = 1:3
%             %solve for axis
%             x1 = YY_1(1,ik); y1 = YY_1(2,ik); z1 = YY_1(3,ik);
%             x2 = YY_2(1,ik); y2 = YY_2(2,ik); z2 = YY_2(3,ik);
% 
%             sB(ik,:) = [(z1-z2-y1+y2) (x1-x2-z1+z2) (y1-y2-x1+x2)];
%         end
%         sA = sB \ [0 0 0]'
%         
        p_A = zeros(length(XX),9);
        p_B = zeros(length(XX),1);
        for i=1:length(XX)
            pi = (i-1)*3;
            p_A(pi+1,:) = [ YY_1(1,i) 0 0 YY_1(1,i) 0 0 YY_1(1,i) 0 0];
            p_A(pi+2,:) = [ 0 YY_1(2,i) 0 0 YY_1(2,i) 0 0 YY_1(2,i) 0];
            p_A(pi+3,:) = [ 0 0 YY_1(3,i) 0 0 YY_1(3,i) 0 0 YY_1(3,i)];
            
            p_B(pi+1) = YY_2(1,i);
            p_B(pi+2) = YY_2(2,i);
            p_B(pi+3) = YY_2(3,i);            
        end
        
        %p_A\p_B
        %first plane
        P12 = [YY_1(1,1)-YY_1(1,2),YY_1(2,1)-YY_1(2,2),YY_1(3,1)-YY_1(3,2)];
        P13 = [YY_1(1,3)-YY_1(1,1),YY_1(2,3)-YY_1(2,1),YY_1(3,3)-YY_1(3,1)];

        Pn = cross(P12,P13);

        Pd = Pn(1)*YY_1(1,1)+Pn(2)*YY_1(2,1)+Pn(3)*YY_1(3,1);

        %second plane
        Q12 = [YY_2(1,1)-YY_2(1,2),YY_2(2,1)-YY_2(2,2),YY_2(3,1)-YY_2(3,2)];
        Q13 = [YY_2(1,3)-YY_2(1,1),YY_2(2,3)-YY_2(2,1),YY_2(3,3)-YY_2(3,1)];

        Qn = cross(Q12,Q13);

        Qd = Qn(1)*YY_2(1,1)+Qn(2)*YY_2(2,1)+Qn(3)*YY_2(3,1);


        % now the intersecting line
        Ln = cross(Pn,Qn);
        
        % find the angle inbetween
        if ((ii-jj)==1)
            angle = acos(dot(Pn,Qn)/(norm(Pn)*norm(Qn)));
            180*(angle/pi) 
        end
        
        x = 0;
        z = (Pd/Pn(2) - Qd/Qn(2))/(Pn(3)/Pn(2) - Qn(3)/Qn(2));
        y = (Pd - Pn(3)*z)/Pn(2);

% 
%         plot3([0 10*Pn(1)/Pn(3)],[0 10*Pn(2)/Pn(3)],[0 10*Pn(3)/Pn(3)],'g');
%         plot3([0 10*Qn(1)/Qn(3)],[0 10*Qn(2)/Qn(3)],[0 10*Qn(3)/Qn(3)],'b');
        
%        [ii jj]
        if (Ln(3)>0) 
            plot3([0 100*Ln(1)/abs(Ln(3))],[0 100*Ln(2)/abs(Ln(3))],[0 100*Ln(3)/abs(Ln(3))],'r');
            Full(p,:)=Ln;
            p = p + 1;
        end
    end
    end
end
end
Ln = mean(Full);
plot3([0 100*Ln(1)/Ln(3)],[0 100*Ln(2)/Ln(3)],[0 100*Ln(3)/Ln(3)],'g');
%normed = sqrt(Ln(1)^2 + Ln(2)^2 + Ln(3)^2);
Ln = Ln/norm(Ln);
disp ([' axis of rotation = ' num2str(Ln(1)) ' ' num2str(-Ln(3)) ' ' num2str(Ln(2))]);
sA = [Ln(1) -Ln(3) Ln(2)];
save axis.txt sA -ascii